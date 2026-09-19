# Spring AI Examples — Ordered Learning Plan

Modules ordered from simplest to most complex, based on prerequisites (API keys needed), moving parts (single LLM call vs. multi-agent vs. protocol servers), and external dependencies (Docker, npx, vector stores).

Tick a box once you've run and tried a module.

## Tier 0 — Absolute basics (single LLM call, no tools)
- [ ] [kotlin/kotlin-hello-world](kotlin/kotlin-hello-world) — simplest possible Spring AI call, just to confirm your API key/setup works
- [x] [models/chat/helloworld](models/chat/helloworld) — basic `ChatClient` usage patterns (ported from OpenAI to Claude/Anthropic)
- [x] [misc/spring-ai-java-function-callback](misc/spring-ai-java-function-callback) — one function callback, no protocol overhead
- [x] [misc/pizza-tools](misc/pizza-tools) — tools. MCP server example

## Tier 1 — Core concepts (still one app, one model, no external servers)
- [x] [prompt-engineering/prompt-engineering-patterns](prompt-engineering/prompt-engineering-patterns) — prompting techniques
- [x] [prompt-engineering/prompt-engineering-caching](prompt-engineering/prompt-engineering-caching) — prompting techniques with caching
- [x] [misc/openai-streaming-response](misc/openai-streaming-response) — streaming output
- [ ] [kotlin/kotlin-function-callback](kotlin/kotlin-function-callback) — Kotlin variant of tool calling
- [x] [advisors/recursive-advisor-demo](advisors/recursive-advisor-demo)
- [x] [advisors/tool-argument-augmenter-demo](advisors/tool-argument-augmenter-demo)
- [x] [advisors/evaluation-recursive-advisor-demo](advisors/evaluation-recursive-advisor-demo) — advisor chain concepts, still single-process

## Image Processing (vision models predicting self-checkout areas)
- [x] [image-processing/ollama-vision-detection](image-processing/ollama-vision-detection) — local/open model via Ollama (default `qwen2.5vl:7b`)
- [x] [image-processing/claude-vision-detection](image-processing/claude-vision-detection) — Claude vision via Anthropic (cloud), same detect/visualize CLI for side-by-side comparison

## Tier 2 — MCP fundamentals (one client ↔ one server, auto-configured)
- [x] [model-context-protocol/client-starter/starter-default-client](model-context-protocol/client-starter/starter-default-client) — simplest MCP client
- [x] [model-context-protocol/client-starter/starter-webflux-client](model-context-protocol/client-starter/starter-webflux-client) — same idea, reactive
- [x] [model-context-protocol/weather/starter-stdio-server](model-context-protocol/weather/starter-stdio-server) — simplest MCP *server*
- [x] [model-context-protocol/weather/starter-webmvc-server](model-context-protocol/weather/starter-webmvc-server)
- [ ] [model-context-protocol/weather/starter-webflux-server](model-context-protocol/weather/starter-webflux-server)
- [ ] [model-context-protocol/weather/manual-webflux-server](model-context-protocol/weather/manual-webflux-server) — manual (non-starter) wiring, more code to understand
- [ ] [model-context-protocol/filesystem](model-context-protocol/filesystem) — MCP filesystem server example

## Tier 3 — MCP with real external APIs + memory
- [ ] [model-context-protocol/web-search/brave-starter](model-context-protocol/web-search/brave-starter) — one-shot Brave Search, needs `BRAVE_API_KEY` + npx
- [ ] [model-context-protocol/web-search/brave-chatbot](model-context-protocol/web-search/brave-chatbot) — same but interactive + conversation memory
- [ ] [misc/claude-skills-demo](misc/claude-skills-demo)

## Tier 4 — Agentic workflow patterns (multi-step LLM orchestration, single process)
- [ ] [agentic-patterns/chain-workflow](agentic-patterns/chain-workflow) — sequential pipeline
- [ ] [agentic-patterns/routing-workflow](agentic-patterns/routing-workflow) — classify-then-dispatch
- [ ] [agentic-patterns/parallelization-workflow](agentic-patterns/parallelization-workflow) — fan-out/fan-in
- [ ] [agents/reflection](agents/reflection) — self-critique loop
- [ ] [agentic-patterns/evaluator-optimizer](agentic-patterns/evaluator-optimizer) — generator+critic loop
- [ ] [agentic-patterns/orchestrator-workers](agentic-patterns/orchestrator-workers) — dynamic task decomposition, closest to a true agent swarm

## Tier 5 — Advanced MCP protocol features (multi-module, more moving parts)
- [ ] [model-context-protocol/mcp-annotations/mcp-annotations-server](model-context-protocol/mcp-annotations/mcp-annotations-server) + [mcp-annotations-client](model-context-protocol/mcp-annotations/mcp-annotations-client) — annotation-driven MCP
- [ ] [model-context-protocol/sampling/mcp-sampling-server](model-context-protocol/sampling/mcp-sampling-server) + [mcp-sampling-client](model-context-protocol/sampling/mcp-sampling-client) — server-initiated LLM sampling (harder MCP concept)
- [ ] [model-context-protocol/sampling-annotations/mcp-sampling-server-annotations](model-context-protocol/sampling-annotations/mcp-sampling-server-annotations) + [client](model-context-protocol/sampling-annotations/mcp-sampling-client-annotations)
- [ ] [model-context-protocol/dynamic-tool-update/server](model-context-protocol/dynamic-tool-update/server) + [client](model-context-protocol/dynamic-tool-update/client) — tools that change at runtime
- [ ] [model-context-protocol/mcp-apps-server](model-context-protocol/mcp-apps-server) — note: Gradle-based, not Maven, so build tooling differs from the rest

## Tier 6 — Infra-heavy / most complex
- [ ] [model-context-protocol/brave-docker-agents-gateway](model-context-protocol/brave-docker-agents-gateway) — Docker-based agents gateway, extra infra dependency
- [ ] [kotlin/rag-with-kotlin](kotlin/rag-with-kotlin) — RAG: embeddings + vector store setup, most moving parts of any module here

---

**Suggested approach:** work through Tier 0–2 in order to build fluency with `ChatClient`, tool calling, and MCP basics; move quickly through Tier 3 since it repeats earlier concepts; spend real time on Tier 4 (the agentic patterns) for multi-agent/orchestration understanding; treat Tier 5–6 as optional deep-dives once the fundamentals are solid.
