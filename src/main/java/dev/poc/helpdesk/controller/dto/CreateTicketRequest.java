package dev.poc.helpdesk.controller.dto;

import dev.poc.helpdesk.domain.enumerator.Category;
import dev.poc.helpdesk.domain.enumerator.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTicketRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotNull Priority priority,
        @NotNull Category category
) {}