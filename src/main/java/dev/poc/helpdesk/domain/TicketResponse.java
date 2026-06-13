package dev.poc.helpdesk.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ticket_responses")
public class TicketResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Column(nullable = false, length = 4000)
    private String body;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected TicketResponse() {
    }

    public TicketResponse(String body, String author) {
        this.body = body;
        this.author = author;
        this.createdAt = Instant.now();
    }

    void attachTo(Ticket ticket) {
        this.ticket = ticket;
    }

    public Long getId() { return id; }
    public String getBody() { return body; }
    public String getAuthor() { return author; }
    public Instant getCreatedAt() { return createdAt; }
}