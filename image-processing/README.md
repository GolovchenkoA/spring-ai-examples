# Image Processing

Vision model experiments: detecting areas (bagging, scanning, table) in a
photo of a self-checkout station, using local/open models via Ollama - no
cloud API key required.

Each sub-folder here is its own standalone Maven module, registered directly
in the root [pom.xml](../pom.xml) (same convention as `advisors/*` and
`prompt-engineering/*`), so different vision models/approaches can be
compared side by side.

## Implementations

- [ollama-vision-detection](ollama-vision-detection) — Ollama + a local vision model
  (default `qwen2.5vl:7b`, swappable to `llava`, `llama3.2-vision`, `moondream2`, etc.)
- [claude-vision-detection](claude-vision-detection) — Claude vision via Anthropic (cloud)

Both expose the same `detect`/`visualize` CLI, so the same photo can be run through
each and the resulting `.areas.json` files compared directly.

More implementations can be added as sibling modules under this folder.
