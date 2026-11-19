package com.example.ticket.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ticket.domain.Ticket;
import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class TicketRepositoryFilterTest {

    @Autowired private TicketRepository ticketRepository;

    @BeforeEach
    void seed() {
        ticketRepository.deleteAll();
        saveTicket("TKT-1001", "A", TicketStatus.OPEN, TicketPriority.HIGH, "alice", "billing");
        saveTicket("TKT-1002", "B", TicketStatus.OPEN, TicketPriority.MEDIUM, "bob", "auth");
        saveTicket("TKT-1003", "C", TicketStatus.RESOLVED, TicketPriority.LOW, "alice", "billing");
    }

    @Test
    void filtersByStatusAndAssigneeWithAndSemantics() {
        var spec =
                TicketSpecifications.combine(
                        TicketStatus.OPEN, null, "alice", null);
        var results =
                ticketRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTicketId()).isEqualTo("TKT-1001");
    }

    @Test
    void filtersByCategory() {
        var spec = TicketSpecifications.combine(null, null, null, "billing");
        var results = ticketRepository.findAll(spec);

        assertThat(results).extracting(Ticket::getTicketId).containsExactlyInAnyOrder("TKT-1001", "TKT-1003");
    }

    private void saveTicket(
            String ticketId,
            String title,
            TicketStatus status,
            TicketPriority priority,
            String assignee,
            String category) {
        Instant now = Instant.now();
        Ticket ticket = new Ticket();
        ticket.setTicketId(ticketId);
        ticket.setTitle(title);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setAssignee(assignee);
        ticket.setCategory(category);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticketRepository.save(ticket);
    }
}