# Ollama Vision Detection

Detects areas (bagging, scanning, table) in a photo of a self-checkout station
using a local Ollama vision model — no cloud API key needed. Two commands:

1. **`detect`** — scan a photo, output a JSON description of the areas found
   (source image dimensions + each area's name and pixel bounding box). The
   source image itself is never modified.
2. **`visualize`** — take a photo + a `detect` JSON output, draw the predicted
   areas on a *new* annotated image so you can visually check whether the
   prediction is correct.

## Prerequisites

- Java 17+
- [Ollama](https://ollama.com) running locally, with a vision-capable model pulled:
  ```bash
  ollama pull qwen2.5vl:7b
  ```

## Configuration

```properties
spring.ai.ollama.chat.model=qwen2.5vl:7b
```

Swap models with no code changes — just pull a different one and update this
property:
```bash
ollama pull llava                 # spring.ai.ollama.chat.model=llava
ollama pull llama3.2-vision:11b   # spring.ai.ollama.chat.model=llama3.2-vision:11b
ollama pull moondream2            # spring.ai.ollama.chat.model=moondream2
```

## Running

**Case 1 — detect areas:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="detect path/to/checkout.jpg"
```
Writes `path/to/checkout.jpg.areas.json` (or pass a second argument for a
custom output path), e.g.:
```json
{
  "sourceImage": { "width": 1920, "height": 1080 },
  "areas": [
    { "name": "scanning", "x": 640, "y": 200, "width": 400, "height": 350 },
    { "name": "bagging",  "x": 1100, "y": 220, "width": 420, "height": 360 }
  ]
}
```

**Case 2 — visualize the prediction:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="visualize path/to/checkout.jpg path/to/checkout.jpg.areas.json"
```
Draws a colored, labeled box for each area onto a copy of the image
(`checkout-annotated.png` by default), saves it, and tries to open it with
your OS's default image viewer.

## Accuracy expectations

This uses a general-purpose vision-language model prompted to estimate
bounding boxes, not a trained/fine-tuned object detector. Treat results as a
best-effort visual estimate — good for quickly comparing how different local
models perform on this task, not for production-grade spatial precision.

## Related

- [misc/openai-streaming-response](../../misc/openai-streaming-response) — the
  provider-agnostic `ChatModel` pattern used here
- [Ollama vision models](https://ollama.com/search?c=vision) — browse other
  local models to try
