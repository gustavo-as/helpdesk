package dev.poc.helpdesk.controller;

import dev.poc.helpdesk.domain.Ticket;
import dev.poc.helpdesk.domain.enumerator.TicketStatus;
import dev.poc.helpdesk.service.TicketService;
import dev.poc.helpdesk.controller.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import dev.poc.helpdesk.ai.DraftService;
import dev.poc.helpdesk.controller.dto.ReplyDraft;
import dev.poc.helpdesk.domain.Ticket;
import dev.poc.helpdesk.ai.AgentService;
import dev.poc.helpdesk.ai.AgentOutcome;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService service;
    private final DraftService draftService;
    private final AgentService agentService;

    @Value("${helpdesk.agent.enabled:false}")
    private boolean agentEnabled;

    public TicketController(TicketService service, DraftService draftService, AgentService agentService) {
        this.service = service;
        this.draftService = draftService;
        this.agentService = agentService;
    }

    @PostMapping("/{id}/draft-reply")
    public ReplyDraft draftReply(@PathVariable Long id) {
        Ticket ticket = service.get(id);
        return new ReplyDraft(draftService.draftReply(ticket));
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

    @PostMapping("/{id}/agent")
    public ResponseEntity<AgentOutcome> runAgent(@PathVariable Long id) {
        if (!agentEnabled) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        return ResponseEntity.ok(agentService.handle(id));
    }
}