/*
* Copyright 2024 - 2024 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.agentic;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.util.Assert;

/**
 * Routing workflow pattern (see {@code agentic-patterns/routing-workflow}), adapted to
 * run entirely offline against a local Ollama server and to route each category to a
 * different local model.
 *
 * <p>
 * One Ollama server can host several pulled models at once, so "different model per
 * route" is just a per-request {@link OllamaChatOptions} override on a single
 * {@link ChatClient} - no need for a separate client per model. This lets simple
 * categories use a small, fast model and categories that need more reasoning use a
 * bigger one, all offline.
 *
 * @see Route
 * @see <a href="https://www.anthropic.com/research/building-effective-agents">Building
 * Effective Agents</a>
 */
public class RoutingWorkflow {

	private final ChatClient chatClient;

	/** Model used to classify the input into a route; kept small since classifying
	 * a category is much easier than answering the ticket. */
	private final String classifierModel;

	public RoutingWorkflow(ChatClient chatClient, String classifierModel) {
		this.chatClient = chatClient;
		this.classifierModel = classifierModel;
	}

	/**
	 * Classifies the input, then answers it using the route's own prompt and model.
	 * @param input  the input text to be routed and processed
	 * @param routes map of route names to their specialized prompt and model
	 * @return the response from the selected route's model
	 */
	public String route(String input, Map<String, Route> routes) {
		Assert.notNull(input, "Input text cannot be null");
		Assert.notEmpty(routes, "Routes map cannot be null or empty");

		String routeKey = determineRoute(input, routes.keySet());
		Route selected = routes.get(routeKey);
		if (selected == null) {
			throw new IllegalArgumentException("Selected route '" + routeKey + "' not found in routes map");
		}

		return chatClient.prompt(selected.prompt() + "\nInput: " + input)
			.options(OllamaChatOptions.builder().model(selected.model()))
			.call()
			.content();
	}

	private String determineRoute(String input, Iterable<String> availableRoutes) {
		System.out.println("\nAvailable routes: " + availableRoutes);

		String selectorPrompt = String.format("""
				Analyze the input and select the most appropriate support team from these options: %s
				Respond with a JSON object with exactly these two fields:
				- "reasoning": brief explanation of why this ticket belongs to that team
				- "selection": the chosen team name, exactly as given above

				Input: %s""", availableRoutes, input);

		// Small local models are unreliable at free-form JSON formatting - "json" forces
		// Ollama's constrained decoding so the output always parses, even for a 1B model.
		RoutingResponse routingResponse = chatClient.prompt(selectorPrompt)
			.options(OllamaChatOptions.builder().model(classifierModel).format("json"))
			.call()
			.entity(RoutingResponse.class);

		System.out.println(
				String.format("Routing Analysis:%s\nSelected route: %s", routingResponse.reasoning(), routingResponse.selection()));

		return routingResponse.selection();
	}

}
