package dev.poc.helpdesk.controller;

import dev.poc.helpdesk.domain.Ticket;
import dev.poc.helpdesk.domain.enumerator.TicketStatus;
import dev.poc.helpdesk.service.TicketService;
import dev.poc.helpdesk.controller.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TicketView> create(@Valid @RequestBody CreateTicketRequest req) {
        Ticket ticket = service.open(req.title(), req.description(), req.priority(), req.category());
        return ResponseEntity
                .created(URI.create("/api/tickets/" + ticket.getId()))
                .body(TicketView.from(ticket));
    }

    @GetMapping
    public List<TicketSummaryView> list(@RequestParam(required = false) TicketStatus status) {
        List<Ticket> tickets = (status == null) ? service.list() : service.listByStatus(status);
        return tickets.stream().map(TicketSummaryView::from).toList();
    }

    @GetMapping("/{id}")
    public TicketView get(@PathVariable Long id) {
        return TicketView.from(service.get(id));
    }

    @PostMapping("/{id}/assign")
    public TicketView assign(@PathVariable Long id, @Valid @RequestBody AssignRequest req) {
        return TicketView.from(service.assign(id, req.agent()));
    }

    @PostMapping("/{id}/transition")
    public TicketView transition(@PathVariable Long id, @Valid @RequestBody TransitionRequest req) {
        return TicketView.from(service.transition(id, req.target()));
    }

    @PostMapping("/{id}/responses")
    public TicketView respond(@PathVariable Long id, @Valid @RequestBody AddResponseRequest req) {
        return TicketView.from(service.respond(id, req.body(), req.author()));
    }
}