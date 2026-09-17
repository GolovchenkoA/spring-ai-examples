# Prompt Engineering — Caching Comparison

Measures the actual token and cost impact of Anthropic prompt caching by running
the same sequence of requests twice against the same large, static few-shot
system prompt — once with caching **disabled**, once with caching **enabled**
(`AnthropicCacheStrategy.SYSTEM_ONLY`) — and printing a side-by-side comparison.

## Why

A few-shot prompt (instructions + examples) is often static — only the user's
question changes between requests. Without caching, that static block is
re-sent and re-billed as full-price input tokens on *every single request*.
This example makes that cost difference concrete instead of theoretical.

## What it measures

For each call, Anthropic's response reports three separate token counts that
don't overlap:
- `input_tokens` — fresh, non-cached input processed this turn
- `cache_creation_input_tokens` — tokens written to the cache (cache miss)
- `cache_read_input_tokens` — tokens served from the cache (cache hit, billed
  far cheaper than a fresh read)

The app sums these across N calls for each scenario and estimates a dollar
cost using hardcoded `$/MTok` pricing constants (see the comment at the top of
[PromptEngineeringCachingApplication.java](src/main/java/org/springframework/ai/example/prompt_engineering_caching/PromptEngineeringCachingApplication.java) —
check [anthropic.com/pricing](https://www.anthropic.com/pricing) before
trusting these for real budgeting, prices change over time).

## Running

```bash
export ANTHROPIC_API_KEY=<your-key>
./mvnw spring-boot:run
```

Pass a repeat count to see the savings compound (the cache-write surcharge on
the first call means caching only pays off from the 2nd reused call onward):

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=20
```

## Things to watch for

- **The default cache TTL is short (~5 minutes)** — if calls are slow (rate
  limits, retries), a later call might miss the cache and trigger another
  "write" instead of a "read." Run it without long pauses between calls.
- **The first cached call is more expensive, not cheaper** — that's expected;
  it's paying the write surcharge to populate the cache. The savings appear
  starting from the 2nd call.

## Related

- [misc/pizza-tools](../../misc/pizza-tools) — this module used to be an MCP
  server exposing a `pizzaOrderTool`; that's now its own separate project.
- [prompt-engineering/prompt-engineering-patterns](../prompt-engineering-patterns) —
  general prompt engineering patterns, including the original (uncached)
  version of this same few-shot pizza-order example.
