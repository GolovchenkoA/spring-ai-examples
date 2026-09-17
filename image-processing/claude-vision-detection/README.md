# Claude Vision Detection

Detects areas (bagging, scanning, table) in a photo of a self-checkout station
using Claude's vision capability via Anthropic. Same Case 1/Case 2 design as
[ollama-vision-detection](../ollama-vision-detection), swapped to a cloud
model instead of a local one — meant for side-by-side comparison.

1. **`detect`** — scan a photo, output a JSON description of the areas found
   (source image dimensions + each area's name and pixel bounding box). The
   source image itself is never modified.
2. **`visualize`** — take a photo + a `detect` JSON output, draw the predicted
   areas on a *new* annotated image so you can visually check whether the
   prediction is correct.

## Prerequisites

- Java 17+
- Anthropic API key

## Configuration

```properties
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY}
spring.ai.anthropic.chat.options.model=claude-sonnet-4-5-20250929
```

## Running

```bash
export ANTHROPIC_API_KEY=<your-key>
```

**Case 1 — detect areas:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="detect C:\Users\User\IdeaProjects\spring-ai-examples\image-processing\images\photo1.jpg"
```
Writes `path/to/checkout.jpg.areas.json` (or pass a second argument for a
custom output path).

**Case 2 — visualize the prediction:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="visualize C:\Users\User\IdeaProjects\spring-ai-examples\image-processing\images\photo1.jpg C:\Users\User\IdeaProjects\spring-ai-examples\image-processing\images\photo1.jpg.areas.json"
```
Draws a colored, labeled box for each area onto a copy of the image
(`checkout-annotated.png` by default), saves it, and tries to open it with
your OS's default image viewer.

## Comparing against the local model

Run `detect` with the same photo through both this module and
[ollama-vision-detection](../ollama-vision-detection), then diff the two
`.areas.json` outputs to see how a cloud model vs. a local open model
estimate the same areas.

## Accuracy expectations

This uses a general-purpose vision-language model prompted to estimate
bounding boxes, not a trained/fine-tuned object detector. Treat results as a
best-effort visual estimate, not production-grade spatial precision.
