package com.example.ragembeddings.model;

import java.time.Instant;

/**
 * A stored question/answer pair, embedded (via its {@code title}) and persisted in the vector store.
 */
public record QaItem(String id, String title, String fullQuestion, String answer, Instant createdAt) {
}
