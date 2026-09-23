package com.example.agentic;

/**
 * A routing target: a specialized system prompt paired with the local Ollama model
 * that should answer it. Different routes can use different models (e.g. a small,
 * fast model for simple categories and a bigger one for categories that need more
 * reasoning), since one Ollama server can host several pulled models at once.
 *
 * @param prompt the specialized system prompt for this route
 * @param model  the Ollama model tag to use for this route (must already be pulled,
 *               e.g. via {@code ollama pull llama3.1:8b})
 */
public record Route(String prompt, String model) {
}
