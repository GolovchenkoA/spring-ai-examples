package com.example.ragembeddings.model;

/**
 * A past question suggested as a live-search match while the user is typing a new one.
 */
public record SuggestionItem(String id, String title, double score) {
}
