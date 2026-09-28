# MCP Sampling: @McpSampling vs. McpClientCustomizer

Two different ways to register a client-side sampling handler in Spring AI's
MCP client support. Both solve the same problem — the server asks the
client's connected LLM for a completion — but they plug in at different
levels.

`@McpSampling`'s `clients()` attribute is `String[]`, so one method can
target multiple named clients at once (`@McpSampling(clients = {"server1",
"server2"})`) — but there's no wildcard for "all clients"; names must be
listed explicitly.

## `@McpSampling` — when the logic is self-contained per connection

```java
@McpSampling(clients = "server1")
public CreateMessageResult samplingHandler(CreateMessageRequest llmRequest) { ... }
```

**Use it when:**
- Each named connection needs its own (or a shared, explicitly-listed)
  handler, and that's the only customization that connection needs.
- The handler is a pure function of the request — no need to touch other
  client-spec settings (transport timeouts, other builder options).
- You want the cleaner, declarative style consistent with this repo's other
  annotation-driven code (`@McpTool`, `@McpPrompt` on the server side use the
  same idiom).
- You're registering multiple, independent capabilities (sampling,
  elicitation, logging, progress) — each as its own separate method/class,
  like `McpClientHandlerProviders`
  (`model-context-protocol/mcp-annotations/mcp-annotations-client`) does with
  four separate annotated methods.

## `@Bean McpClientCustomizer<McpClient.SyncSpec>` — when you need the raw builder or shared state across callbacks

```java
@Bean
McpClientCustomizer<McpClient.SyncSpec> samplingCustomizer(Map<String, ChatClient> chatClients) {
    return (name, mcpClientSpec) -> {
        mcpClientSpec.loggingConsumer(...);
        mcpClientSpec.sampling(...);
    };
}
```

**Use it when:**
- You need direct access to the `McpClient.SyncSpec` builder itself — for
  settings `@McpSampling` doesn't expose at all (this is the only route to
  raw spec-level configuration).
- You want to bundle several callback registrations together in one place,
  sharing the same closure/dependencies — `mcp-sampling-client`'s
  (`model-context-protocol/sampling/mcp-sampling-client`) customizer wires
  both `loggingConsumer` and `sampling` in one bean, both able to reference
  the same injected `Map<String, ChatClient> chatClients`.
- The customization needs to apply based on the connection's name at
  construction time (the `(name, spec) -> ...` signature gives you that name
  directly, letting you branch differently per client inside one function)
  rather than being pre-bound to fixed names via an annotation attribute.
- Your handler logic needs richer Spring-managed dependencies injected as
  constructor/method parameters on the bean itself (like the
  `Map<String, ChatModel> chatModels` → `chatClients` map) rather than just
  what fits cleanly into a single annotated method's parameter list.

## The rule of thumb

If you're writing one clear handler per named connection, reach for
`@McpSampling` first — it's simpler and matches this repo's dominant style.
Reach for `McpClientCustomizer` when you need the builder itself, or when
one function needs to configure multiple aspects of the client together
using shared state — exactly the situation `mcp-sampling-client` was in:
routing to a dynamically selected real model based on request content,
sharing that model-lookup map between both the sampling and logging
registrations.

## Worked example: `mcp-sampling-client`'s real sampling handler

`model-context-protocol/sampling/mcp-sampling-client/.../McpClientApplication.java`
is a genuine, working sampling handler (unlike `McpClientHandlerProviders`'s
canned-response example) — worth reading alongside this comparison:

```java
@Bean
public Map<String, ChatClient> chatClients(List<ChatModel> chatModels) {
    return chatModels.stream()
        .collect(Collectors.toMap(model -> model.getClass().getSimpleName().toLowerCase(),
                model -> ChatClient.builder(model).build()));
}

@Bean
McpClientCustomizer<McpClient.SyncSpec> samplingCustomizer(Map<String, ChatClient> chatClients) {
    return (name, mcpClientSpec) -> {
        mcpClientSpec.loggingConsumer(logingMessage ->
                System.out.println("MCP LOGGING: [" + logingMessage.level() + "] " + logingMessage.data()));

        mcpClientSpec.sampling(llmRequest -> {
            var userPrompt = ((TextContent) llmRequest.messages().get(0).content()).text();
            String modelHint = llmRequest.modelPreferences().hints().get(0).name();

            ChatClient hintedChatClient = chatClients.entrySet().stream()
                .filter(e -> e.getKey().contains(modelHint))
                .findFirst().orElseThrow().getValue();

            String response = hintedChatClient.prompt()
                .system(llmRequest.systemPrompt())
                .user(userPrompt)
                .call().content();

            return CreateMessageResult.builder(Role.ASSISTANT, TextContent.builder(response).build(), modelHint)
                .build();
        });
    };
}
```

`chatClients` collects every auto-configured `ChatModel` bean (this module
has both OpenAI and Anthropic starters active) into a map keyed by class
name, e.g. `{"openaichatmodel" -> ChatClient, "anthropicchatmodel" ->
ChatClient}`.

The `sampling(...)` handler reads the model hint from the incoming request
(e.g. `"openai"` or `"anthropic"`, sent by the server's
`WeatherService.callMcpSampling`), finds the matching entry via
`.contains(modelHint)`, and actually calls that real model — returning a
genuine, provider-specific response rather than a stub.

The same bean also registers `loggingConsumer(...)`, printing every
`exchange.loggingNotification(...)` sent by the server
(`"MCP LOGGING: [level] message"`) — concrete proof that whether a logging
notification is ever seen depends entirely on whether the client bothered to
register a handler for it.

### Full round trip this enables

1. A main, OpenAI-driven `ChatClient` is asked about the weather.
2. It calls the server's `getTemperature` tool.
3. Inside that tool, the server fires two sampling requests back to this
   client — hinted `"openai"` and `"anthropic"`.
4. This client's `sampling(...)` handler routes each to the correspondingly
   hinted real model, getting back two genuinely different responses (e.g.
   two different poems about the same weather data).
5. The server bundles both responses plus the raw weather JSON into the
   tool's result.
6. That flows back into the main conversation, which can further synthesize
   across all of it.

So this single client file demonstrates a three-model round trip (main
model + two sampled models), orchestrated entirely through MCP tool calls
and sampling.
