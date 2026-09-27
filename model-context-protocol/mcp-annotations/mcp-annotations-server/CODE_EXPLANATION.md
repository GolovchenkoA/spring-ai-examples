# Code Explanation: mcp-annotations-server

## How the classes relate to `McpServerApplication`

`McpServerApplication` is just a plain `@SpringBootApplication` — no wiring logic itself. All the `providers/*` classes are `@Service` beans that get **auto-discovered** by the MCP server auto-configuration (from `spring-ai-mcp-annotations`/`spring-ai-starter-mcp-server*`), which scans for `@McpTool`, `@McpPrompt`, `@McpResource`, `@McpComplete` methods and registers them as MCP capabilities. `McpServerApplication` only adds one manual bean on top of that auto-discovery.

| Class | Role |
|---|---|
| `ToolProvider` | Simple, realistic `@McpTool` example — calls a weather REST API, returns a record. Baseline case. |
| `ToolProvider2` | Service disabled!!! Same tool concept, but demonstrates the **low-level** MCP SDK API (`McpSyncServerExchange`) for server→client features: logging, progress, ping, elicitation, sampling. Commented out (`// @Service`), so not actually registered. |
| `ToolProvider3` | Service enabled!!! The **same** logging/progress/ping/elicitation/sampling demo as `ToolProvider2`, rewritten against a newer, higher-level **context-object API** (`McpSyncRequestContext`) instead of the raw exchange — a cleaner alternative to `ToolProvider2`. Active (`@Service`). |
| `AsyncToolProvider` | Same demo again, but **reactive** (`Mono<String>`, `McpAsyncRequestContext`) — the async counterpart to `ToolProvider3`. |
| `PromptProvider` | `@McpPrompt` methods — reusable prompt templates exposed via MCP, separate capability from tools. |
| `PromptProvider2` | The same PromptProvider , but using Java 25 |
| `CompletionProvider` | `@McpComplete` methods — argument-autocompletion support (e.g. suggesting values while a client fills in a prompt/tool argument). |
| `DocumentProvider` | Mixes `@McpTool`, `@McpPrompt`, and `@McpResource` together — reads files from disk, the most "combined capabilities" example. |
| `UserProfileResourceProvider` | `@McpResource` only — exposes readable data (an in-memory user-profile map) as an MCP resource, not a tool. |
| `SpringAiToolProvider` | The odd one out — uses plain Spring AI **`@Tool`** (not `@McpTool`). Per its own comment in `McpServerApplication`, it's there to show that a regular Spring AI tool provider can *also* be bridged into MCP via `MethodToolCallbackProvider`, alongside the native `@McpTool` beans. |

**In short:** `ToolProvider`/`PromptProvider`/`CompletionProvider`/`DocumentProvider`/`UserProfileResourceProvider` each demonstrate one distinct MCP *capability type* (tools, prompts, completion, resources); `ToolProvider2`/`ToolProvider3`/`AsyncToolProvider` are three progressively-refined variants of the *same* "tool talks back to the client" demo (raw exchange → context API → async context API); `SpringAiToolProvider` shows the non-`@McpTool` interop path.
