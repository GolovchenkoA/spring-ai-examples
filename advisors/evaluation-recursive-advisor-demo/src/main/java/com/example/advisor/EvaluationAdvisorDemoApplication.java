package com.example.advisor;

import java.util.Random;

import tools.jackson.databind.ObjectMapper;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EvaluationAdvisorDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvaluationAdvisorDemoApplication.class, args);
	}

	/**
	 * Anthropic official courses. Evaluation workflow
	 *    https://academy.claude.com/courses/building-with-the-claude-api/a-typical-eval-workflow
	 *    https://academy.claude.com/courses/building-with-the-claude-api/running-the-eval
	 * @param anthropicChatModel
	 * @param ollamaChatModel
	 * @return
	 */

	@Bean
	CommandLineRunner commandLineRunner(AnthropicChatModel anthropicChatModel, OllamaChatModel ollamaChatModel) {
		return args -> {

			ChatClient chatClient = ChatClient.builder(anthropicChatModel) // @formatter:off
					.defaultTools(new MyTools())
					.defaultAdvisors(

//			it uses the Ollama-backed client purely to evaluate/critique the Anthropic-backed client's answer
//			(rating it, retrying up to maxRepeatAttempts(15) until it scores successRating(4) or better).
//			So one model generates, a different (here, local/free) model judges — a deliberate separation of
//			"the model doing the task" from "the model grading the task."
						SelfRefineEvaluationAdvisor.builder()
							.chatClientBuilder(ChatClient.builder(ollamaChatModel))
							.maxRepeatAttempts(15)
							.successRating(4)
							.order(0)
							.build(),
						
						new MyLoggingAdvisor(2))
				.build(); 
				
				var answer = chatClient
					.prompt("What is current weather in Paris?")
					.call()
					.content();

				// @formatter:on

			System.out.println(answer);
		};
	}

	static class MyTools {

		final int[] temperatures = {-125, 15, -255};
		private final Random random = new Random();
		
		@Tool(description = "Get the current weather for a given location")
		public String weather(String location) {
			int temperature = temperatures[random.nextInt(temperatures.length)];
			System.out.println(">>> Tool Call responseTemp: " + temperature);
			return "The current weather in " + location + " is sunny with a temperature of " + temperature + "°C.";
		}
	}

	static class MyLoggingAdvisor implements BaseAdvisor {

        private ObjectMapper objectMapper = new ObjectMapper();

		private final int order;

		public MyLoggingAdvisor(int order) {
			this.order = order;
		}	

		@Override
		public int getOrder() {
			return this.order;
		}

		@Override
		public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
			print("REQUEST", chatClientRequest.prompt().getInstructions());
			return chatClientRequest;
		}

		@Override
		public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
			print("RESPONSE", chatClientResponse.chatResponse().getResults());
			return chatClientResponse;
		}

		private void print(String label, Object object) {
			System.out.println(label + ":" + this.objectMapper.writeValueAsString(object) + "\n");
		}

	}

}
