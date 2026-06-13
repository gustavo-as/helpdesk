package dev.poc.helpdesk.controller.dto;

import dev.poc.helpdesk.domain.TicketResponse;

import java.time.Instant;

public record ResponseView(Long id, String body, String author, Instant createdAt) {

    public static ResponseView from(TicketResponse r) {
        return new ResponseView(r.getId(), r.getBody(), r.getAuthor(), r.getCreatedAt());
    }
}