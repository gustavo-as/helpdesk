package dev.poc.helpdesk.domain;

import dev.poc.helpdesk.domain.enumerator.Category;
import dev.poc.helpdesk.domain.enumerator.Priority;
import dev.poc.helpdesk.domain.enumerator.TicketStatus;
import dev.poc.helpdesk.exception.InvalidTransitionException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private String assignee;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private Instant slaDueAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TicketResponse> responses = new ArrayList<>();

    protected Ticket() {
    }

    public Ticket(String title, String description, Priority priority, Category category) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.status = TicketStatus.OPEN;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        this.slaDueAt = this.createdAt.plus(priority.sla());
    }

    public void transitionTo(TicketStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new InvalidTransitionException(this.status, target);
        }
        this.status = target;
        touch();
    }

    public void assignTo(String agent) {
        this.assignee = agent;
        touch();
    }

    public void addResponse(TicketResponse response) {
        response.attachTo(this);
        this.responses.add(response);
        touch();
    }

    public boolean isBreachingSla(Instant now) {
        return this.status != TicketStatus.RESOLVED
                && this.status != TicketStatus.CLOSED
                && now.isAfter(this.slaDueAt);
    }

    private void touch() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TicketStatus getStatus() { return status; }
    public Priority getPriority() { return priority; }
    public Category getCategory() { return category; }
    public String getAssignee() { return assignee; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getSlaDueAt() { return slaDueAt; }
    public List<TicketResponse> getResponses() { return responses; }
}