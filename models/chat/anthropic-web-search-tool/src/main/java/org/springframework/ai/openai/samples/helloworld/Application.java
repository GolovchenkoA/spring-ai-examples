package org.springframework.ai.openai.samples.helloworld;

import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.AnthropicWebSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner cli(ChatClient.Builder builder, ConfigurableApplicationContext context) {
        return args -> {
            // Claude has no built-in internet access, regardless of which model
            // (Opus/Sonnet/Haiku) is used. Anthropic's server-side web search tool
            // gives it that ability. Note: web_search can only be called
            // indirectly (from code Claude writes), so tool_choice can't force it
            // directly - leave tool choice on auto and let Claude decide when a
            // question actually needs a real web lookup vs. what it already knows.

            AnthropicChatOptions.Builder chatOptions = AnthropicChatOptions.builder()
                    .webSearchTool(AnthropicWebSearchTool.builder()
                            .maxUses(3).build());

            var chat = builder
                    .defaultSystem("""
                            You have access to a web search tool. Use it whenever a question depends on \
                            current, real-time, or otherwise unknown information (news, prices, recent \
                            events, current date, etc.) rather than relying only on what you already know.""")
                    .defaultOptions(chatOptions)
                    .build();
            var question = args.length > 0 ? String.join(" ", args) : "What is the current date now";
            System.out.println("USER: " + question);
            System.out.println("ASSISTANT: " +
                    chat.prompt(question).call().content());
            System.out.println("\nAnthropicWebSearchTool demo completed!");
            context.close();
        };
    }
}
