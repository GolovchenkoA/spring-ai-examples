package org.springframework.ai.example.prompt_engineering_caching;

import java.util.List;

import org.springframework.ai.anthropic.AnthropicCacheOptions;
import org.springframework.ai.anthropic.AnthropicCacheStrategy;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Compares Anthropic prompt caching vs. no caching by sending the same
 * sequence of requests twice against the same large, static few-shot system
 * prompt - once with caching disabled, once with AnthropicCacheStrategy.SYSTEM_ONLY -
 * and reports actual token usage and estimated cost for each scenario.
 *
 * Anthropic's usage response separates input_tokens (fresh, non-cached),
 * cache_creation_input_tokens (written to cache) and cache_read_input_tokens
 * (served from cache) - they don't overlap, so summing their costs doesn't
 * double-count.
 */
@SpringBootApplication
public class PromptEngineeringCachingApplication {

	// Pricing per million tokens for claude-sonnet-4-5 (see application.properties).
	// The 1.25x / 0.1x cache write/read multipliers are Anthropic's standard
	// published ratios for the 5-minute cache TTL; the base $/MTok figures below
	// can change over time - check https://www.anthropic.com/pricing before
	// trusting these numbers for real budgeting.
	private static final double INPUT_PRICE_PER_MTOK = 3.00;
	private static final double OUTPUT_PRICE_PER_MTOK = 15.00;
	private static final double CACHE_WRITE_PRICE_PER_MTOK = INPUT_PRICE_PER_MTOK * 1.25;
	private static final double CACHE_READ_PRICE_PER_MTOK = INPUT_PRICE_PER_MTOK * 0.10;

	private static final String FEW_SHOT_SYSTEM_PROMPT = """
			Parse a customer's pizza order into valid JSON.

			EXAMPLE 1:
			I want a small pizza with cheese, tomato sauce, and pepperoni.
			JSON Response:
			```
			{
				"size": "small",
				"type": "normal",
				"ingredients": ["cheese", "tomato sauce", "pepperoni"]
			}
			```

			EXAMPLE 2:
			Can I get a large pizza with tomato sauce, basil and mozzarella.
			JSON Response:
			```
			{
				"size": "large",
				"type": "normal",
				"ingredients": ["tomato sauce", "basil", "mozzarella"]
			}
			```

			EXAMPLE 3:
			I would like a medium pizza, half pepperoni and half mushroom, with extra cheese.
			JSON Response:
			```
			{
				"size": "medium",
				"type": "half-and-half",
				"ingredients": ["pepperoni", "mushroom", "extra cheese"]
			}
			```

			Respond with JSON only, no other commentary.
			""";

	private static final List<String> QUESTIONS = List.of(
			"I want a large pizza with pepperoni, mushrooms, and extra cheese.",
			"Can I get a small pizza with just cheese and basil?",
			"I'd like a medium pizza, half sausage and half pineapple.",
			"Give me a large vegetarian pizza with olives, peppers, and onions.",
			"One small pizza with pepperoni and jalapenos, please.");

	public static void main(String[] args) {
		SpringApplication.run(PromptEngineeringCachingApplication.class, args);
	}

	@Bean
	CommandLineRunner cli(ChatClient.Builder chatClientBuilder, ConfigurableApplicationContext context) {
		return args -> {
			int repeats = args.length > 0 ? Integer.parseInt(args[0]) : QUESTIONS.size();

			System.out.println("=== Scenario A: caching DISABLED (" + repeats + " calls) ===");
			ScenarioTotals withoutCaching = runScenario(chatClientBuilder, false, repeats);

			System.out.println("\n=== Scenario B: caching ENABLED, SYSTEM_ONLY (" + repeats + " calls) ===");
			ScenarioTotals withCaching = runScenario(chatClientBuilder, true, repeats);

			printComparison(withoutCaching, withCaching);

			context.close();
		};
	}

	private ScenarioTotals runScenario(ChatClient.Builder chatClientBuilder, boolean cachingEnabled, int repeats) {
		AnthropicCacheOptions cacheOptions = cachingEnabled
				? AnthropicCacheOptions.builder().strategy(AnthropicCacheStrategy.SYSTEM_ONLY).build()
				: AnthropicCacheOptions.disabled();

		ChatClient chatClient = chatClientBuilder.clone()
				.defaultSystem(FEW_SHOT_SYSTEM_PROMPT)
				.defaultOptions(AnthropicChatOptions.builder()
						.cacheOptions(cacheOptions)
						.maxTokens(200))
				.build();

		ScenarioTotals totals = new ScenarioTotals();
		for (int i = 0; i < repeats; i++) {
			String question = QUESTIONS.get(i % QUESTIONS.size());
			ChatResponse response = chatClient.prompt(question).call().chatResponse();
			Usage usage = response.getMetadata().getUsage();
			totals.add(usage);
			System.out.printf("  call %d -> promptTokens=%d, completionTokens=%d, cacheWrite=%d, cacheRead=%d%n",
					i + 1, usage.getPromptTokens(), usage.getCompletionTokens(),
					nullToZero(usage.getCacheWriteInputTokens()), nullToZero(usage.getCacheReadInputTokens()));
		}
		return totals;
	}

	private void printComparison(ScenarioTotals withoutCaching, ScenarioTotals withCaching) {
		double costWithout = withoutCaching.estimatedCost();
		double costWith = withCaching.estimatedCost();
		double savings = costWithout - costWith;
		double savingsPct = costWithout == 0 ? 0 : (savings / costWithout) * 100;

		System.out.println("\n=== Comparison ===");
		System.out.printf("%-18s | %14s | %11s | %10s | %10s%n", "Scenario", "promptTokens", "cacheWrite",
				"cacheRead", "est. cost");
		System.out.printf("%-18s | %14d | %11d | %10d | $%9.4f%n", "No caching", withoutCaching.promptTokens,
				withoutCaching.cacheWriteTokens, withoutCaching.cacheReadTokens, costWithout);
		System.out.printf("%-18s | %14d | %11d | %10d | $%9.4f%n", "Caching enabled", withCaching.promptTokens,
				withCaching.cacheWriteTokens, withCaching.cacheReadTokens, costWith);
		System.out.printf("%nSavings: $%.4f (%.1f%%)%n", savings, savingsPct);
		System.out.println("""

				Note: the first call in the caching scenario is MORE expensive than a
				plain call (cache-write surcharge). Savings only show up from the 2nd
				reused call onward - pass a larger repeat count as the first argument
				(e.g. "20") to see the effect compound.
				""");
	}

	private static long nullToZero(Long value) {
		return value == null ? 0L : value;
	}

	private static class ScenarioTotals {

		long promptTokens;
		long completionTokens;
		long cacheWriteTokens;
		long cacheReadTokens;

		void add(Usage usage) {
			promptTokens += nullToZero(usage.getPromptTokens() == null ? null : usage.getPromptTokens().longValue());
			completionTokens += nullToZero(
					usage.getCompletionTokens() == null ? null : usage.getCompletionTokens().longValue());
			cacheWriteTokens += nullToZero(usage.getCacheWriteInputTokens());
			cacheReadTokens += nullToZero(usage.getCacheReadInputTokens());
		}

		double estimatedCost() {
			double inputCost = (promptTokens / 1_000_000.0) * INPUT_PRICE_PER_MTOK;
			double cacheWriteCost = (cacheWriteTokens / 1_000_000.0) * CACHE_WRITE_PRICE_PER_MTOK;
			double cacheReadCost = (cacheReadTokens / 1_000_000.0) * CACHE_READ_PRICE_PER_MTOK;
			double outputCost = (completionTokens / 1_000_000.0) * OUTPUT_PRICE_PER_MTOK;
			return inputCost + cacheWriteCost + cacheReadCost + outputCost;
		}

	}

}
