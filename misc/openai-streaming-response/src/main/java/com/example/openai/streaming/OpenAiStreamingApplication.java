package com.example.openai.streaming;

import reactor.core.publisher.Flux;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class OpenAiStreamingApplication {
	public static void main(String[] args) {
		SpringApplication.run(OpenAiStreamingApplication.class, args);
	}
}

@RestController
@RequestMapping("/ai")
class ChatController {

	// ChatModel is the provider-agnostic interface - Spring Boot autoconfigures
	// whichever implementation matches the model starter on the classpath
	// (OpenAiChatModel, AnthropicChatModel, etc.), so this controller doesn't
	// need to know or care which provider is actually wired in.
	private final ChatModel chatModel;

	public ChatController(ChatModel chatModel) {
		this.chatModel = chatModel;
	}

	@GetMapping(value = "/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ChatResponse> generateStream(
			@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
		return chatModel.stream(new Prompt(new UserMessage(message)));
	}
}
