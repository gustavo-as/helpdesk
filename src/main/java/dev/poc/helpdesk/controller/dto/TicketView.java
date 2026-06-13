package dev.poc.helpdesk.controller.dto;

import dev.poc.helpdesk.domain.Ticket;
import dev.poc.helpdesk.domain.enumerator.Category;
import dev.poc.helpdesk.domain.enumerator.Priority;
import dev.poc.helpdesk.domain.enumerator.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketView(
        Long id,
        String title,
        String description,
        TicketStatus status,
        List<TicketStatus> allowedNext,
        Priority priority,
        Category category,
        String assignee,
        Instant createdAt,
        Instant updatedAt,
        Instant slaDueAt,
        boolean slaBreached,
        List<ResponseView> responses
) {
    public static TicketView from(Ticket t) {
        return new TicketView(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                List.copyOf(t.getStatus().allowedNext()),
                t.getPriority(),
                t.getCategory(),
                t.getAssignee(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                t.getSlaDueAt(),
                t.isBreachingSla(Instant.now()),
                t.getResponses().stream().map(ResponseView::from).toList()
        );
    }
}