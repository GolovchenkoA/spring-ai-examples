# Routing Workflow Pattern - Offline (Ollama)

A variant of [`agentic-patterns/routing-workflow`](../routing-workflow) that runs entirely
offline against a local [Ollama](https://ollama.com) server, and routes each category to a
**different local model** - a small/fast model for simple categories, a bigger one for
categories that need more reasoning.

## Why this works with one `ChatClient`

Unlike the Anthropic/OpenAI-hosted variant (where each provider account has one model
active per API key/config), Ollama serves *every model you've pulled* from the same local
server. So "different model per route" doesn't require multiple `ChatClient` beans - it's
just a per-request `OllamaChatOptions` override on a single client:

```java
chatClient.prompt(route.prompt() + "\nInput: " + input)
    .options(OllamaChatOptions.builder().model(route.model()))
    .call()
    .content();
```

See [`RoutingWorkflow.java`](src/main/java/com/example/agentic/RoutingWorkflow.java) and
[`Route.java`](src/main/java/com/example/agentic/Route.java) - a `Route` pairs a
specialized prompt with the model tag that should answer it.

## Setup

1. Install and start [Ollama](https://ollama.com/download).
2. Run:
   ```bash
   ./mvnw spring-boot:run
   ```

No API key needed - `application.properties` points at `http://localhost:11434`. Both
`llama3.2:1b` and `llama3.1:8b` are pulled/verified automatically at startup via
`spring.ai.ollama.init.*` (`chat.additional-models` covers the second model, since only
the default `spring.ai.ollama.chat.model` would otherwise be auto-pulled) - no manual
`ollama pull` needed. The first run will take longer while both models download.

## What it does

Three sample support tickets are classified by the small model, then answered by
whichever model that ticket's route is configured for. Swap the model tags in
`Application.java` for any models you have pulled locally.

## Reference

- [Building Effective Agents](https://www.anthropic.com/research/building-effective-agents) - Anthropic Research
- [Spring AI Ollama docs](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html)
