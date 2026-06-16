package dev.poc.helpdesk.ai;

import dev.poc.helpdesk.domain.Ticket;
import dev.poc.helpdesk.domain.enumerator.TicketStatus;
import dev.poc.helpdesk.service.TicketService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private static final String AGENT_AUTHOR = "AI Agent";

    private final ChatClient chatClient;
    private final TicketService ticketService;

    public AgentService(ChatClient.Builder chatClientBuilder, TicketService ticketService) {
        this.chatClient = chatClientBuilder.build();
        this.ticketService = ticketService;
    }

    public AgentOutcome handle(Long ticketId) {
        Ticket ticket = ticketService.get(ticketId);
        AgentDecision decision = decide(ticket);

        if (!"RESOLVE".equalsIgnoreCase(safe(decision.action()))) {
            return escalate(ticket, decision);
        }
        return resolve(ticket, decision);
    }

    private AgentDecision decide(Ticket ticket) {
        return chatClient.prompt()
                .system("""
                        You are a support agent deciding whether you can fully resolve a ticket on your own.
                        Resolve ONLY simple, low-risk tickets you can answer correctly and completely.
                        If the ticket is ambiguous, sensitive, needs account changes, or you are unsure, escalate.
                        Provide:
                        - action: "RESOLVE" or "ESCALATE"
                        - reply: if resolving, the full reply to the customer (same language as the ticket); otherwise empty
                        - reason: if escalating, a short note for the human; otherwise empty
                        """)
                .user(context(ticket))
                .call()
                .entity(AgentDecision.class);
    }

    private AgentOutcome resolve(Ticket ticket, AgentDecision decision) {
        String reply = safe(decision.reply());
        if (reply.isBlank()) {
            return escalate(ticket, new AgentDecision("ESCALATE", "", "Agent produced no reply."));
        }
        ticketService.respond(ticket.getId(), reply, AGENT_AUTHOR);
        Ticket resolved = driveToResolved(ticket);
        return new AgentOutcome("RESOLVED", reply, resolved.getStatus());
    }

    private AgentOutcome escalate(Ticket ticket, AgentDecision decision) {
        String reason = safe(decision.reason()).isBlank()
                ? "Escalated for human review."
                : safe(decision.reason());
        ticketService.respond(ticket.getId(), "[Internal] " + reason, AGENT_AUTHOR);
        return new AgentOutcome("ESCALATED", reason, ticket.getStatus());
    }

    private Ticket driveToResolved(Ticket ticket) {
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket = ticketService.transition(ticket.getId(), TicketStatus.IN_PROGRESS);
        }
        if (ticket.getStatus() == TicketStatus.IN_PROGRESS) {
            ticket = ticketService.transition(ticket.getId(), TicketStatus.RESOLVED);
        }
        return ticket;
    }

    private String context(Ticket ticket) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(ticket.getTitle()).append("\n");
        sb.append("Description: ").append(ticket.getDescription()).append("\n");
        if (!ticket.getResponses().isEmpty()) {
            sb.append("\nConversation so far:\n");
            ticket.getResponses().forEach(r ->
                    sb.append("- ").append(r.getAuthor()).append(": ").append(r.getBody()).append("\n"));
        }
        return sb.toString();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}