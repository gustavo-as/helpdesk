package dev.poc.helpdesk.controller.dto;

import dev.poc.helpdesk.domain.enumerator.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionRequest(@NotNull TicketStatus target) {}