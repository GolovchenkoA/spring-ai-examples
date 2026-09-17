package org.springframework.ai.example.prompt_engineering_caching;

import java.util.List;
import java.util.UUID;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

@Service
public class PizzaTools {

	public record PizzaOrderResponse(String orderId, String size, List<String> ingredients, double totalPrice,
			int estimatedDeliveryMinutes) {
	}

	@McpTool(name = "pizzaOrderTool",
			description = "Places a pizza order and returns an order confirmation with price and estimated delivery time.")
	public PizzaOrderResponse pizzaOrderTool(
			@McpToolParam(description = "Pizza size: small, medium, or large", required = true) String size,
			@McpToolParam(description = "List of ingredients/toppings to include", required = true) List<String> ingredients) {

		double basePrice = switch (size.toLowerCase()) {
			case "small" -> 8.0;
			case "large" -> 14.0;
			default -> 11.0; // medium
		};
		double totalPrice = basePrice + ingredients.size() * 1.5;
		String orderId = "PZ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

		return new PizzaOrderResponse(orderId, size, ingredients, totalPrice, 30);
	}

}
