package dev.poc.helpdesk.repository;

import dev.poc.helpdesk.domain.enumerator.Category;
import dev.poc.helpdesk.domain.Ticket;
import dev.poc.helpdesk.domain.enumerator.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByAssignee(String assignee);
    List<Ticket> findByCategory(Category category);

    @Query("select t from Ticket t left join fetch t.responses where t.id = :id")
    Optional<Ticket> findByIdWithResponses(Long id);
}