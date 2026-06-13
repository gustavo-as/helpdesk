package dev.poc.helpdesk.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignRequest(@NotBlank String agent) {}