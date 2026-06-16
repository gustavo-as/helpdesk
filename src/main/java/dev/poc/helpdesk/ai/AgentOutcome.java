package dev.poc.helpdesk.ai;

import dev.poc.helpdesk.domain.enumerator.TicketStatus;

public record AgentOutcome(String result, String message, TicketStatus status) {}