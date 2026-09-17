# Spring Boot AI Streaming Integration

This Spring Boot application demonstrates real-time, cloud-agnostic streaming integration with Spring AI. It provides a simple endpoint that streams AI responses using Spring WebFlux. The controller depends only on the provider-agnostic `org.springframework.ai.chat.model.ChatModel` interface — swapping providers (OpenAI, Anthropic, etc.) is a matter of swapping the Maven starter dependency and API key property, with no code changes.

## Prerequisites

- Java 17 or higher
- Maven
- An API key for whichever provider's starter is on the classpath (this example is configured for Anthropic/Claude by default)

## Configuration

This example currently depends on `spring-ai-starter-model-anthropic` in `pom.xml`. Set your key:

```properties
spring.ai.anthropic.api-key=your-api-key-here
```

**To switch providers** (e.g. back to OpenAI): swap the `spring-ai-starter-model-anthropic` dependency in `pom.xml` for `spring-ai-starter-model-openai`, and set `spring.ai.openai.api-key` instead. Keep only **one** model-provider starter on the classpath at a time — with more than one present, Spring can't resolve which implementation to inject for the plain `ChatModel` type and throws `NoUniqueBeanDefinitionException`.

## Running the Application

1. Clone this repository
2. Configure your API key as described above
3. Run the application:
```bash
./mvnw spring-boot:run
```

The application will start on port 8080 by default.

## Usage

The application exposes a streaming endpoint that returns AI responses as a reactive stream.

### Endpoint

```
GET /ai/generateStream
```

#### Parameters
- `message` (optional): The input message to send to the model
    - Default value: "Tell me a joke"

#### Testing with curl

You can test the streaming endpoint using curl. The following command will stream the response and format it using `jq`:

```bash
curl localhost:8080/ai/generateStream | sed 's/data://' | jq .
```

## Implementation Details

The application uses:
- Spring WebFlux for reactive streaming
- Spring AI's provider-agnostic `ChatModel` interface for AI model interaction

The main components are:
1. `OpenAiStreamingApplication`: The Spring Boot application entry point
2. `ChatController`: REST controller handling the streaming endpoint

### Code Details

The streaming endpoint is implemented as follows:

```java
@GetMapping(value = "/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<ChatResponse> generateStream(
       @RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
    return chatModel.stream(new Prompt(new UserMessage(message)));
}
```

Key points:
- The endpoint uses `MediaType.TEXT_EVENT_STREAM_VALUE` to indicate that the response will be streamed as text events. 
- Returns a `Flux<ChatResponse>` which is Spring WebFlux's reactive type for handling a stream of multiple elements.
- Takes an optional `message` parameter with a default value of "Tell me a joke".
- Uses Spring AI's `chatModel.stream()` method which returns a reactive stream of responses from whichever provider is configured.
- The `Prompt` and `UserMessage` classes are provided by Spring AI to structure the request to the model.

When called, this endpoint:
1. Takes the user's input message (or uses the default)
2. Wraps it in a Spring AI `Prompt` object
3. Streams the AI's response back to the client in real-time
4. Each response chunk is automatically serialized to JSON
