package com.example.ragembeddings.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the RAG + vector store playground.
 *
 * @param titleLength           number of characters of the question used as the embedded/stored title
 * @param similarityThreshold   minimum cosine similarity for a past question to be suggested
 * @param suggestionTopK        max number of suggestions returned per live-search request
 * @param minSuggestionLength   minimum input length (chars) before live suggestions are triggered
 * @param storePath             file path where the vector store is persisted as JSON
 */
@ConfigurationProperties(prefix = "rag")
public record RagProperties(
		int titleLength,
		double similarityThreshold,
		int suggestionTopK,
		int minSuggestionLength,
		String storePath) {

	public RagProperties {
		if (titleLength <= 0) {
			titleLength = 100;
		}
		if (similarityThreshold <= 0) {
			similarityThreshold = 0.78;
		}
		if (suggestionTopK <= 0) {
			suggestionTopK = 5;
		}
		if (minSuggestionLength <= 0) {
			minSuggestionLength = 10;
		}
		if (storePath == null || storePath.isBlank()) {
			storePath = "./data/vector-store.json";
		}
	}
}
