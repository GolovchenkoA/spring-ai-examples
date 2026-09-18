# LLM vs SLM vs LVM vs Segmentation Models

A practical guide to the model types referenced throughout this repo — what
each one actually does, what it's good/bad at, and which one to reach for
depending on the task. Written with this repo's own
[image-processing](image-processing) modules and
[advisors/evaluation-recursive-advisor-demo](advisors/evaluation-recursive-advisor-demo)
as running examples, since that's where these distinctions actually matter
in practice.

## LLM — Large Language Model

```
LLM
Claude, GPT, Llama, Mistral, etc.
text → LLM → text
```

**What it is:** a model trained to predict/generate text, token by token,
from other text. Pure LLMs have no notion of pixels or images at all — input
and output are both text.

**Examples:** Claude (text-only calls), GPT, Llama, Mistral, Qwen (text mode).

**Good at:**
- Conversation, reasoning, summarization, classification of text
- Structured output (JSON, following a schema)
- Tool/function calling
- Code generation

**Not good at / can't do at all:**
- Understanding an image — a pure LLM literally cannot accept image input
- Anything requiring pixel-level or spatial precision

**Used in this repo:** [models/chat/helloworld](models/chat/helloworld),
[misc/spring-ai-java-function-callback](misc/spring-ai-java-function-callback),
[agentic-patterns](agentic-patterns), [advisors](advisors) — every module
that only ever sends/receives text.

## SLM — Small Language Model

```
SLM
Phi-3/Phi-4 mini, Gemma 3 4B, Llama 3.2 3B, flowaicom-flow-judge, etc.
text → SLM → text
```

**What it is:** architecturally the same idea as an LLM (a text-in,
text-out transformer), just far fewer parameters — typically under ~10B,
sometimes as small as 1-3B, versus the hundreds of billions in a frontier
LLM. The distinction is size/cost/deployability, not a different technique.

**Examples:** Phi-3/Phi-4 mini, Gemma 3 4B, Llama 3.2 3B, Qwen small
variants — and, relevant to this repo, the `avcodes/flowaicom-flow-judge:q4`
model used as the judge in
[advisors/evaluation-recursive-advisor-demo](advisors/evaluation-recursive-advisor-demo)
is exactly this: a small model purpose-built for one narrow job (scoring),
not general-purpose chat.

**Good at:**
- Running locally on modest hardware (laptop CPU, small GPU) — no cloud
  dependency, no API cost, low latency
- Narrow, well-defined tasks it's specifically tuned for (classification,
  scoring/judging, simple extraction, a fixed-format response)
- High-volume/high-frequency calls where per-call cost or latency of a large
  cloud model would be prohibitive

**Not good at:**
- Broad general reasoning, nuanced instruction-following, long multi-step
  agentic tasks, or tasks outside what it was specifically tuned for
- Being the "generator" in a generator/evaluator setup where quality of the
  actual output matters most — this repo deliberately keeps Claude (a full
  LLM) as the generator and only uses the SLM for the narrower judging role

**Used in this repo:**
[advisors/evaluation-recursive-advisor-demo](advisors/evaluation-recursive-advisor-demo) —
Claude (LLM) generates the answer, a small local judge model (SLM, via
Ollama) scores it. This split exists specifically because the judge only
needs to do one narrow thing (rate 1-4) repeatedly and cheaply, not because
an SLM is a good all-purpose substitute for an LLM.

## LVM / VLM — (Large) Vision-Language Model

```
VLM
Qwen-VL, LLaVA, Gemini, Claude's vision capabilities, etc.
image + text → VLM → text/JSON
```

**What it is:** an LLM extended to also accept image input alongside text.
Under the hood it's still fundamentally a text-generation model — the image
is encoded into the same token/embedding space the model already reasons
over, so its output is still free-form text (or structured JSON), not pixel
data.

**Examples:** Claude with vision (used in
[claude-vision-detection](image-processing/claude-vision-detection)),
GPT-4o/GPT-5 vision, Gemini, LLaVA, Qwen-VL (used in
[ollama-vision-detection](image-processing/ollama-vision-detection)),
Moondream.

**Good at:**
- Describing/captioning an image
- Zero-shot classification ("is this a cat or a dog?")
- Answering questions about image content
- Rough spatial reasoning — "roughly where is X in this image?"

**Not good at:**
- **Pixel-precise localization.** An LVM can *estimate* a bounding box or
  outline by describing coordinates in text, but it's guessing based on
  general visual understanding, not measuring — there's no guarantee the
  numbers it outputs are accurate. This is exactly why both vision-detection
  modules in this repo document their output as a "best-effort visual
  estimate, not a precision measurement."
- Consistent, repeatable pixel boundaries across runs
- Any task where you need a hard guarantee about where an object starts and
  ends, not just an educated guess

**Used in this repo:**
[image-processing/ollama-vision-detection](image-processing/ollama-vision-detection),
[image-processing/claude-vision-detection](image-processing/claude-vision-detection) —
both ask an LVM to estimate area outlines as 4 points; both are explicitly
scoped as *approximate*, not precise, for exactly the reason above.

## Segmentation model

```
Segmentation model
YOLO, Grounding DINO, U-Net, DeepLab, Mask R-CNN, SAM, etc.
image → Segmentation model → boxes/mask (no text)
```

**What it is:** a model trained specifically for **pixel-level** image
tasks — not text generation at all. Its output is a mask (which exact pixels
belong to which object/class), not a sentence or a JSON blob describing
coordinates. This is a fundamentally different model architecture from an
LLM/LVM, usually trained end-to-end on labeled pixel masks rather than on
text.

**Sub-types worth knowing apart:**

| Type | Output | Example models |
|---|---|---|
| **Object detection** | Bounding boxes + class labels (still just boxes, no pixel mask) | YOLO, Grounding DINO |
| **Semantic segmentation** | Every pixel labeled by class (e.g. "floor", "person") — doesn't distinguish two instances of the same class | U-Net, DeepLab |
| **Instance segmentation** | Every pixel labeled by class *and* which specific instance it belongs to | Mask R-CNN, SAM (Segment Anything) |

**Good at:**
- Exact, pixel-accurate boundaries
- Reliable, repeatable measurements (given the same input, same output)
- Tasks where downstream code needs to know precisely which pixels to
  crop/mask/measure

**Not good at:**
- Reasoning, conversation, following arbitrary natural-language instructions
- Zero-shot generalization to a class it wasn't trained/prompted for (though
  newer "promptable" segmentation models like SAM narrow this gap somewhat)
- Producing a text explanation of *why* it segmented something a certain way

**Not currently used in this repo** — the `image-processing` modules
deliberately use LVMs instead, trading pixel precision for zero setup (no
training data, no separate model to host) and natural-language flexibility
(the "table must be right of bagging" spatial-relationship prompting only
works because it's a language model, not a segmentation model).

## When to use what

| Need | Use |
|---|---|
| Chat, reasoning, tool calling, structured text output — no image involved | **LLM** |
| Same as above, but narrow/repetitive/high-volume, and it must run locally or cheaply | **SLM** |
| "What's in this image?" / rough classification / approximate spatial description | **LVM** |
| Exact pixel boundaries, reliable/repeatable measurements, cropping/masking | **Segmentation model** |
| Natural-language-flexible reasoning *about* rough regions ("the zone to the right of X") | **LVM** — this is why the `image-processing` modules use one |
| Scoring/grading another model's output (LLM-as-a-judge) | **SLM** — see [advisors/evaluation-recursive-advisor-demo](advisors/evaluation-recursive-advisor-demo); avoids the cost of a full LLM call per evaluation and avoids the generator grading its own work |
| Production system that must guarantee spatial accuracy (e.g. a robot arm using coordinates to physically act) | **Segmentation/detection model** — never trust an LVM's coordinates for something with real-world physical consequences |

**Rule of thumb:** if getting the answer *wrong by a few pixels* is
acceptable, an LVM is faster to build with and more flexible. If it isn't —
if something downstream actually depends on the exact boundary — you need a
real segmentation/detection model, possibly combined with an LVM for the
"understand what's being asked" part and the segmentation model for the
"measure it precisely" part. Similarly: reach for an SLM only when the task
is narrow enough that a smaller model can do it reliably — it's a cost/deploy
optimization for a well-scoped job, not a drop-in replacement for an LLM's
general reasoning ability.
