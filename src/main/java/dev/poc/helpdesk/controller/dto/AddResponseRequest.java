package dev.poc.helpdesk.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record AddResponseRequest(
        @NotBlank String body,
        @NotBlank String author
) {}