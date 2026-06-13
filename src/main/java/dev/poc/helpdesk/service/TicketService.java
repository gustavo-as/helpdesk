package dev.poc.helpdesk.service;

import dev.poc.helpdesk.domain.Ticket;
import dev.poc.helpdesk.exception.TicketNotFoundException;
import dev.poc.helpdesk.domain.TicketResponse;
import dev.poc.helpdesk.domain.enumerator.Category;
import dev.poc.helpdesk.domain.enumerator.Priority;
import dev.poc.helpdesk.domain.enumerator.TicketStatus;
import dev.poc.helpdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Ticket open(String title, String description, Priority priority, Category category) {
        return repository.save(new Ticket(title, description, priority, category));
    }

    @Transactional(readOnly = true)
    public List<Ticket> list() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Ticket> listByStatus(TicketStatus status) {
        return repository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Ticket get(Long id) {
        return repository.findByIdWithResponses(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    @Transactional
    public Ticket assign(Long id, String agent) {
        Ticket ticket = get(id);
        ticket.assignTo(agent);
        return ticket;
    }

    @Transactional
    public Ticket transition(Long id, TicketStatus target) {
        Ticket ticket = get(id);
        ticket.transitionTo(target);
        return ticket;
    }

    @Transactional
    public Ticket respond(Long id, String body, String author) {
        Ticket ticket = get(id);
        ticket.addResponse(new TicketResponse(body, author));
        return ticket;
    }
}