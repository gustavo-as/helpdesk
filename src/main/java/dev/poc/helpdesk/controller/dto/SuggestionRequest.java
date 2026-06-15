package dev.poc.helpdesk.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record SuggestionRequest(
        @NotBlank String title,
        @NotBlank String description
) {}