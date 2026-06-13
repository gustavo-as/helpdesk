package dev.poc.helpdesk.repository;

import dev.poc.helpdesk.domain.enumerator.Category;
import dev.poc.helpdesk.domain.Ticket;
import dev.poc.helpdesk.domain.enumerator.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByAssignee(String assignee);
    List<Ticket> findByCategory(Category category);
}