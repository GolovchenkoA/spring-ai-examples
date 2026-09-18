# Ollama Vision Detection

Detects areas (bagging, scanning, table) in a photo of a self-checkout station
using a local Ollama vision model — no cloud API key needed. Takes a single
argument (the image path) and runs both steps in one call:

1. **Detect** — scan the photo, write a JSON description of the areas found
   (source image dimensions + each area's name and 4-point outline). The
   source image itself is never modified.
2. **Visualize** — draw the predicted areas from that JSON onto a *new*
   annotated image, so you can visually check whether the prediction is
   correct.

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

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="path/to/checkout.jpg"
```

This writes `path/to/checkout.jpg.areas.json`, e.g.:
```json
{
  "sourceImage": { "width": 1920, "height": 1080 },
  "areas": [
    { "name": "scanning", "coordinates": [[640, 200], [1040, 200], [1040, 550], [640, 550]] },
    { "name": "bagging",  "coordinates": [[1100, 220], [1520, 220], [1520, 580], [1100, 580]] }
  ]
}
```
Each area's `coordinates` are exactly 4 `[x, y]` points, ordered clockwise
from the top-left corner (top-left, top-right, bottom-right, bottom-left).

...then immediately draws those areas onto a new annotated image
(`checkout-annotated.png` by default), saves it, and tries to open it with
your OS's default image viewer — so you see both the JSON and the visual
check from a single command.

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
