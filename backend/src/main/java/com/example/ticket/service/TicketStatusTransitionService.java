package com.example.ticket.service;

import com.example.ticket.api.dto.TicketResponse;
import com.example.ticket.domain.InvalidStatusTransitionException;
import com.example.ticket.domain.Ticket;
import com.example.ticket.domain.TicketNotFoundException;
import com.example.ticket.domain.TicketStatus;
import com.example.ticket.domain.TicketStatusTransitionPolicy;
import com.example.rag.ingestion.TicketIngestionEventType;
import com.example.rag.ingestion.TicketIngestionPublisher;
import com.example.ticket.repository.TicketRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketStatusTransitionService {

    private static final String TICKET_ID_PATTERN = "^TKT-[0-9]+$";

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final TicketIngestionPublisher ingestionPublisher;

    public TicketStatusTransitionService(
            TicketRepository ticketRepository,
            TicketMapper ticketMapper,
            TicketIngestionPublisher ingestionPublisher) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.ingestionPublisher = ingestionPublisher;
    }

    @Transactional
    public TicketResponse transition(String ticketId, TicketStatus targetStatus) {
        if (ticketId == null || !ticketId.matches(TICKET_ID_PATTERN)) {
            throw new TicketNotFoundException(ticketId);
        }
        Ticket ticket =
                ticketRepository
                        .findByTicketId(ticketId)
                        .orElseThrow(() -> new TicketNotFoundException(ticketId));

        TicketStatus from = ticket.getStatus();
        if (TicketStatusTransitionPolicy.evaluate(from, targetStatus)
                == TicketStatusTransitionPolicy.Decision.REJECTED) {
            throw new InvalidStatusTransitionException(from, targetStatus);
        }

        ticket.setStatus(targetStatus);
        ticket.setUpdatedAt(Instant.now());
        TicketResponse response = ticketMapper.toResponse(ticketRepository.save(ticket));
        ingestionPublisher.publish(ticketId, TicketIngestionEventType.STATUS_CHANGE);
        return response;
    }
}