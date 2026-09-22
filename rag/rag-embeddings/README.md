# RAG + Vector Store Playground

A small standalone web app for experimenting with RAG and vector similarity search.

Two-pane UI: the left panel lists past questions, the (larger) right panel is where you
type a new question and see its answer. As you type, the app live-searches past questions
by embedding similarity and suggests matches before you submit. Click a suggestion to reuse
its stored answer with no LLM call; ignore it and submit, and the question goes to the LLM,
gets answered, and the new Q&A pair is embedded and persisted.

UI components (history sidebar, HTMX + Tailwind layout) are adapted from
[`misc/claude-skills-demo/document-forge`](../../misc/claude-skills-demo/document-forge).

## Tech stack

- Spring Boot (WebFlux) + Spring AI 2.0.0
- Vector store: `SimpleVectorStore` (in-memory, persisted as JSON on disk) - intentionally
  not production-grade, this is a playground
- Embedding model: OpenAI (`text-embedding-3-small` by default)
- Chat model: Anthropic Claude (`claude-sonnet-4-5` by default)
- Frontend: Thymeleaf + HTMX + Tailwind (CDN), same stack as `document-forge`

## Data model

Each stored item (`com.example.ragembeddings.model.QaItem`) has:

- `id`
- `title` - first N characters of the question (`rag.title-length`, default 100); this is
  the text that gets embedded
- `fullQuestion`
- `answer`
- `createdAt`

The embedding itself lives inside the vector store, keyed by the same `id`; `title`,
`fullQuestion`, `answer` and `createdAt` are stored as document metadata so a single
`vectorStore.save()` / `.load()` round-trip persists everything.

## Running it

Requires an Anthropic API key (chat) and an OpenAI API key (embeddings):

```bash
export ANTHROPIC_API_KEY=sk-ant-...
export OPENAI_API_KEY=sk-...
./mvnw spring-boot:run
```

The app starts on <http://localhost:8081>. The vector store is persisted to
`./data/vector-store.json` (configurable, see below) and reloaded on the next startup.

## Configuration (`application.yml`)

| Property | Default | Purpose |
|---|---|---|
| `rag.title-length` | `100` | Characters of the question used as the embedded/stored title |
| `rag.similarity-threshold` | `0.78` | Minimum cosine similarity for a live suggestion |
| `rag.suggestion-top-k` | `5` | Max suggestions returned per live-search request |
| `rag.min-suggestion-length` | `10` | Minimum input length before suggestions are searched |
| `rag.store-path` | `./data/vector-store.json` | Where the vector store JSON is persisted |
| `spring.ai.anthropic.chat.options.model` | `claude-sonnet-4-5` | Chat model used to answer questions |
| `spring.ai.openai.embedding.options.model` | `text-embedding-3-small` | Embedding model |

Live suggestions are debounced client-side via HTMX (`keyup changed delay:400ms` on the
question textarea); the minimum-length check happens server-side.

## Design notes

- Because every stored item is written through `VectorStore.add(...)`, which always
  computes its embedding before persisting, there's no code path that produces a
  metadata-without-embedding entry in this app - so there's no startup "backfill missing
  embeddings" step. If you hand-edit `vector-store.json` and remove an embedding, the
  simplest fix is to re-ask that question so it's re-embedded and re-saved.
- An in-memory cache mirrors the vector store's metadata (hydrated once at startup) so
  listing history or loading a past answer never needs an embedding-model call; only the
  live similarity search does.
