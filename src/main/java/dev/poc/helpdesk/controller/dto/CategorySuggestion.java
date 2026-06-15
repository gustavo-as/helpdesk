package dev.poc.helpdesk.controller.dto;

import dev.poc.helpdesk.domain.enumerator.Category;

public record CategorySuggestion(Category category, boolean available) {

    public static CategorySuggestion of(Category category) {
        return new CategorySuggestion(category, true);
    }

    public static CategorySuggestion unavailable() {
        return new CategorySuggestion(null, false);
    }
}