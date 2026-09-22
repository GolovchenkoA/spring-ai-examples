# LLM Model Families — When to Use

| Model family | Use it when… |
|---|---|
| **Llama** | You want a **general-purpose model** with broad capabilities, strong ecosystem support, and lots of tooling/community resources. |
| **Mistral** | You want **fast, efficient models**, especially for local deployment, general chat, and coding with relatively modest hardware. |
| **Gemma** | You want a **small, lightweight model** for local use, experimentation, or applications where memory/compute are limited. |
| **Qwen** | You need **strong multilingual, coding, or reasoning capabilities**, particularly across a wide range of languages and technical tasks. |
| **DeepSeek** | You want **strong reasoning or coding performance**, especially for technical problems, while keeping the option of local execution. |

## Quick rule of thumb

- 🧠 **General-purpose:** Llama
- ⚡ **Efficiency / speed:** Mistral
- 💻 **Small local model:** Gemma
- 🌍 **Multilingual + coding:** Qwen
- 🔬 **Reasoning + coding:** DeepSeek

> **Note:** The exact choice depends heavily on the specific model/version and parameter size (e.g., 7B vs. 70B), not just the model family.


# LLM Model Families — Best Use Cases

| Model family | Best use cases | Why |
|---|---|---|
| **Llama** | General-purpose AI, chat assistants, document analysis, RAG, content generation, coding | Broad capabilities and a large ecosystem; a good default when you need one versatile model |
| **Mistral** | Fast local assistants, coding, summarization, document processing, RAG, privacy-sensitive applications | Strong performance relative to model size; useful when speed and resource efficiency matter |
| **Gemma** | Lightweight assistants, edge devices (IoT), local experimentation, classification, summarization, simple RAG | Smaller models can be practical when CPU/RAM/VRAM resources are limited |
| **Qwen** | Multilingual applications, coding, structured data extraction, reasoning, agents | Particularly useful when working across multiple languages or technical domains |
| **DeepSeek** | Coding, mathematical reasoning, complex problem solving, technical analysis | Strong reasoning and coding capabilities; larger variants can require considerably more hardware |

## Practical Selection Guide

- **"I want one model that can do almost everything"** → Llama
- **"I want good performance but prioritize speed/efficiency"** → Mistral
- **"My computer has limited RAM/VRAM"** → Gemma
- **"I work with many languages or lots of code"** → Qwen
- **"My main task is difficult reasoning or programming"** → DeepSeek

> **Note:** Model family alone isn't enough to choose. The specific model size, quantization, context length, and available hardware can matter more than the family name.
