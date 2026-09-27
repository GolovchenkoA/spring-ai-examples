/*
* Copyright 2025 - 2025 the original author or authors.
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
package org.springframework.ai.mcp.sample.server.providers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Service;

/**
 * Same prompts as {@link PromptProvider}, rewritten with Java 25 language features.
 * Prompt names are suffixed with "-v2" so both providers can be registered at once
 * for comparison, without colliding on prompt name.
 *
 * Features used, and why:
 * - Pattern matching for switch with guarded patterns and a {@code case null} branch
 *   (finalized in Java 21) replaces the age if/else chain in
 *   {@link #personalizedMessage} and the case-insensitive topic check in
 *   {@link #stringListPrompt} - both read as a single expression instead of a
 *   sequence of statements.
 * - Text blocks (Java 15) replace string concatenation for multi-line content in
 *   {@link #conversationStarter} and {@link #personalizedMessage}.
 * - {@code String.formatted(...)} (Java 15) replaces manual "+" concatenation for
 *   interpolated one-liners.
 * - The Stream API with {@code Collectors.joining} replaces a manual
 *   {@code StringBuilder} loop in {@link #mapArguments}.
 *
 * Newer JEPs finalized in Java 25 - structured concurrency, scoped values,
 * compact source files - are deliberately not used here: this class only builds
 * strings synchronously with no concurrent work or implicit context to pass down
 * a call chain, so none of them would fit a real need.
 *
 * @author Christian Tzolov
 */


//Disabled
//@Service
public class PromptProvider2 {

	@McpPrompt(name = "greeting-v2", description = "A simple greeting prompt")
	public GetPromptResult greetingPrompt(
			@McpArg(name = "name", description = "The name to greet", required = true) String name) {

		return GetPromptResult
			.builder(List.of(new PromptMessage(Role.ASSISTANT,
					TextContent.builder("Hello, %s! Welcome to the MCP system.".formatted(name)).build())))
			.description("Greeting")
			.build();
	}

	@McpPrompt(name = "personalized-message-v2",
			description = "Generates a personalized message based on user information")
	public GetPromptResult personalizedMessage(McpSyncServerExchange exchange,
			@McpArg(name = "name", description = "The user's name", required = true) String name,
			@McpArg(name = "age", description = "The user's age", required = false) Integer age,
			@McpArg(name = "interests", description = "The user's interests", required = false) String interests) {

		exchange.loggingNotification(
				LoggingMessageNotification.builder(LoggingLevel.INFO, "personalized-message event").build());

		String ageInsight = switch (age) {
			case null -> "";
			case Integer a when a < 30 -> "At %d years old, you have so much ahead of you.\n\n".formatted(a);
			case Integer a when a < 60 ->
				"At %d years old, you have gained valuable life experience.\n\n".formatted(a);
			default -> "At %d years old, you have accumulated wisdom to share with others.\n\n".formatted(age);
		};

		String interestInsight = (interests == null || interests.isBlank())
				? ""
				: "Your interest in %s shows your curiosity and passion for learning.\n\n".formatted(interests);

		String message = """

				Hello, %s!
				%s%sI'm here to assist you with any questions you might have about the Model Context Protocol.""".formatted(name, ageInsight, interestInsight);

		return GetPromptResult
			.builder(List.of(new PromptMessage(Role.ASSISTANT, TextContent.builder(message).build())))
			.description("Personalized Message")
			.build();
	}

	@McpPrompt(name = "conversation-starter-v2", description = "Provides a conversation starter with the system")
	public List<PromptMessage> conversationStarter(GetPromptRequest request) {

		var explanation = """
				Great choice! The Model Context Protocol (MCP) is a standardized way for servers \
				to communicate with language models. It provides a structured approach for \
				exchanging information, making requests, and handling responses. \
				What specific aspect would you like to explore first?""";

		return List.of(
				new PromptMessage(Role.ASSISTANT,
						TextContent.builder("Hello! I'm the MCP assistant. How can I help you today?").build()),
				new PromptMessage(Role.USER,
						TextContent.builder("I'd like to learn more about the Model Context Protocol.").build()),
				new PromptMessage(Role.ASSISTANT, TextContent.builder(explanation).build()));
	}

	@McpPrompt(name = "map-arguments-v2", description = "Demonstrates using a map for arguments")
	public GetPromptResult mapArguments(Map<String, Object> arguments) {

		String message = (arguments == null || arguments.isEmpty())
				? "No arguments were provided."
				: "I received the following arguments:\n\n" + arguments.entrySet()
					.stream()
					.map(entry -> "- %s: %s".formatted(entry.getKey(), entry.getValue()))
					.collect(Collectors.joining("\n"));

		return GetPromptResult
			.builder(List.of(new PromptMessage(Role.ASSISTANT, TextContent.builder(message).build())))
			.description("Map Arguments Demo")
			.build();
	}

	@McpPrompt(name = "single-message-v2", description = "Demonstrates returning a single PromptMessage")
	public PromptMessage singleMessagePrompt(
			@McpArg(name = "name", description = "The user's name", required = true) String name) {
		return new PromptMessage(Role.ASSISTANT,
				TextContent.builder("Hello, %s! This is a single message response.".formatted(name)).build());
	}

	@McpPrompt(name = "string-list-v2", description = "Demonstrates returning a list of strings")
	public List<String> stringListPrompt(@McpArg(name = "topic",
			description = "The topic to provide information about", required = true) String topic) {

		return switch (topic) {
			case String t when t.equalsIgnoreCase("MCP") -> List.of(
					"The Model Context Protocol (MCP) is a standardized way for servers to communicate with language models.",
					"It provides a structured approach for exchanging information, making requests, and handling responses.",
					"MCP allows servers to expose resources, tools, and prompts to clients in a consistent way.");
			default -> List.of("I don't have specific information about " + topic + ".",
					"Please try a different topic or ask a more specific question.");
		};
	}

}
