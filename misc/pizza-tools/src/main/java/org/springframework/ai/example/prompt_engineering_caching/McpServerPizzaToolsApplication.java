package org.springframework.ai.example.prompt_engineering_caching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * MCP server exposing {@link PizzaTools#pizzaOrderTool}, meant to be connected
 * from Claude Desktop as a custom Connector (remote MCP server over Streamable
 * HTTP). Unlike a CommandLineRunner demo, this app doesn't call the Anthropic
 * API itself - Claude Desktop's own model is the one calling this tool.
 */
@SpringBootApplication
public class McpServerPizzaToolsApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerPizzaToolsApplication.class, args);
	}

}
