package dev.poc.helpdesk.domain.enumerator;

import java.time.Duration;

public enum Priority {
    LOW(Duration.ofHours(72)),
    MEDIUM(Duration.ofHours(24)),
    HIGH(Duration.ofHours(8)),
    URGENT(Duration.ofHours(2));

    private final Duration sla;

    Priority(Duration sla) {
        this.sla = sla;
    }

    public Duration sla() {
        return sla;
    }
}