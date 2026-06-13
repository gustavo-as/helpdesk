package dev.poc.helpdesk.domain;

import java.util.Set;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public Set<TicketStatus> allowedNext() {
        return switch (this) {
            case OPEN        -> Set.of(IN_PROGRESS, CLOSED);
            case IN_PROGRESS -> Set.of(RESOLVED, OPEN);
            case RESOLVED    -> Set.of(CLOSED, IN_PROGRESS);
            case CLOSED      -> Set.of();
        };
    }

    public boolean canTransitionTo(TicketStatus target) {
        return allowedNext().contains(target);
    }

}