package com.example.ticket.repository;

import com.example.ticket.domain.Ticket;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketId(String ticketId);

    List<Ticket> findByTicketIdIn(Collection<String> ticketIds);

    boolean existsByTicketId(String ticketId);
}