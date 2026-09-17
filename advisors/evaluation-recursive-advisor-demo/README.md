# Spring AI LLM-as-a-Judge Demo

Demonstrates the **LLM-as-a-Judge** pattern in Spring AI 2.0 using a custom `SelfRefineEvaluationAdvisor` — a recursive advisor that evaluates AI responses and retries with feedback until a quality threshold is met.

## How It Works

`SelfRefineEvaluationAdvisor` implements `CallAdvisor` and loops recursively via `callAdvisorChain.copy(this).nextCall()`:

1. Generate a response with the primary model (Anthropic Claude)
2. Evaluate it using a dedicated judge model (Ollama) on a 1–4 scale
3. If rating < `successRating`, augment the prompt with feedback and retry
4. Return when the rating passes or `maxRepeatAttempts` is exhausted

Tool-call responses are skipped automatically — only final text answers are evaluated.

```java
ChatClient chatClient = ChatClient.builder(anthropicChatModel)
    .defaultTools(new MyTools())
    .defaultAdvisors(
        SelfRefineEvaluationAdvisor.builder()
            .chatClientBuilder(ChatClient.builder(ollamaChatModel)) // separate judge
            .maxRepeatAttempts(15)
            .successRating(4)
            .order(0)
            .build(),
        new MyLoggingAdvisor(2))
    .build();
```

The weather tool returns random temperatures (including physically impossible values like -255°C) to intentionally trigger evaluation failures and retries.

## Sample Output

```
>>> Tool Call responseTemp: -255
Evaluation failed on attempt 1: temperature of -255°C is physically impossible.

>>> Tool Call responseTemp: 15
Evaluation passed on attempt 2: Excellent response with realistic weather data.

The current weather in Paris is sunny with a temperature of 15°C.
```

## Prerequisites

- Java 17+, Maven 3.6+
- Anthropic API key
- [Ollama](https://ollama.com) running locally with a judge model

## Setup

```bash
# Pull a judge model
ollama pull avcodes/flowaicom-flow-judge:q4

export ANTHROPIC_API_KEY=your-key
./mvnw spring-boot:run
```

`application.properties` (already configured):
```properties
spring.ai.ollama.chat.model=avcodes/flowaicom-flow-judge:q4
spring.ai.ollama.chat.temperature=0
spring.ai.chat.client.enabled=false
```

## Install Ollama
1. Install Ollama for Windows from ollama.com/download. Installing it also registers it as a background service that starts the local server automatically on localhost:11434.
2. Pull the judge model this project expects (from application.properties):
```
ollama pull avcodes/flowaicom-flow-judge:q4
```
3. Verify the server is up:
```
curl http://localhost:11434
```
4. Should return Ollama is running instead of a connection error.

## Reference

- [Recursive Advisor Demo](../recursive-advisor-demo) — basic recursive patterns
- [Spring AI Advisors](https://docs.spring.io/spring-ai/reference/api/advisors.html)
- [Judge Arena Leaderboard](https://huggingface.co/spaces/AtlaAI/judge-arena)


## Anthropic courses:
Best match I found: **"Building with the Claude API"** on Claude Academy (Anthropic's official video course platform) — it has lessons that walk through exactly this generate → evaluate → grade loop pattern:

- [A typical eval workflow](https://academy.claude.com/courses/building-with-the-claude-api/a-typical-eval-workflow)
- [Running the eval](https://academy.claude.com/courses/building-with-the-claude-api/running-the-eval)
- [Course home](https://academy.claude.com/courses/building-with-the-claude-api)

The whole course catalog is at [academy.claude.com](https://academy.claude.com/all) (formerly hosted at `anthropic.skilljar.com`) if that specific course isn't the one you remember.

A couple of close alternates, in case it's one of these instead:
- **Anthropic Cookbook notebook** (code, not video, but the canonical reference implementation of this exact pattern): [evaluator_optimizer.ipynb](https://github.com/anthropics/claude-cookbooks/blob/main/patterns/agents/evaluator_optimizer.ipynb)
- **GitHub course repo** (text/notebook-based, not video): [anthropics/courses — prompt_evaluations](https://github.com/anthropics/courses/blob/master/prompt_evaluations/README.md)
- **Engineering blog post** describing the same "evaluator-optimizer" workflow pattern conceptually: [Building Effective AI Agents](https://www.anthropic.com/engineering/building-effective-agents)

If none of these are quite it, let me know any detail you remember (instructor, whether it was Python/TypeScript, course title fragment) and I can narrow it down further.

Sources:
- [Building with the Claude API · Claude Academy](https://academy.claude.com/courses/building-with-the-claude-api)
- [A typical eval workflow · Building with the Claude API · Claude Academy](https://academy.claude.com/courses/building-with-the-claude-api/a-typical-eval-workflow)
- [Running the eval · Building with the Claude API · Claude Academy](https://academy.claude.com/courses/building-with-the-claude-api/running-the-eval)
- [claude-cookbooks/patterns/agents/evaluator_optimizer.ipynb](https://github.com/anthropics/claude-cookbooks/blob/main/patterns/agents/evaluator_optimizer.ipynb)
- [courses/prompt_evaluations/README.md · anthropics/courses](https://github.com/anthropics/courses/blob/master/prompt_evaluations/README.md)
- [Building Effective AI Agents \ Anthropic](https://www.anthropic.com/engineering/building-effective-agents)