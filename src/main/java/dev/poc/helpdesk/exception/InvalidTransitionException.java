package dev.poc.helpdesk.exception;

import dev.poc.helpdesk.domain.enumerator.TicketStatus;

public class InvalidTransitionException extends RuntimeException {
    public InvalidTransitionException(TicketStatus from, TicketStatus to) {
        super("Invalid transition: %s -> %s".formatted(from, to));
    }
}