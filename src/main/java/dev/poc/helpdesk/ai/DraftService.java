package dev.poc.helpdesk.ai;

import dev.poc.helpdesk.domain.Ticket;
import dev.poc.helpdesk.domain.TicketResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class DraftService {

    private final ChatClient chatClient;

    public DraftService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String draftReply(Ticket ticket) {
        return chatClient.prompt()
                .system("""
                        You are a customer support agent drafting a reply to a ticket.
                        Write a clear, professional, friendly response in the same language as the ticket.
                        Be concise. Do not invent facts, account details, or promises you cannot back up.
                        Write only the reply body: no subject line, no signature.
                        """)
                .user(buildContext(ticket))
                .call()
                .content();
    }

    private String buildContext(Ticket ticket) {
        StringBuilder sb = new StringBuilder();
        sb.append("Ticket title: ").append(ticket.getTitle()).append("\n");
        sb.append("Description: ").append(ticket.getDescription()).append("\n");
        if (!ticket.getResponses().isEmpty()) {
            sb.append("\nConversation so far:\n");
            for (TicketResponse response : ticket.getResponses()) {
                sb.append("- ").append(response.getAuthor()).append(": ")
                        .append(response.getBody()).append("\n");
            }
        }
        sb.append("\nDraft the next reply to the customer.");
        return sb.toString();
    }
}