package dev.poc.helpdesk.controller.dto;

import dev.poc.helpdesk.domain.Ticket;
import dev.poc.helpdesk.domain.enumerator.Category;
import dev.poc.helpdesk.domain.enumerator.Priority;
import dev.poc.helpdesk.domain.enumerator.TicketStatus;

import java.time.Instant;

public record TicketSummaryView(
        Long id,
        String title,
        TicketStatus status,
        Priority priority,
        Category category,
        String assignee,
        Instant slaDueAt,
        boolean slaBreached
) {
    public static TicketSummaryView from(Ticket t) {
        return new TicketSummaryView(
                t.getId(),
                t.getTitle(),
                t.getStatus(),
                t.getPriority(),
                t.getCategory(),
                t.getAssignee(),
                t.getSlaDueAt(),
                t.isBreachingSla(Instant.now())
        );
    }
}