package com.example.ticket.service;

import com.example.ticket.api.dto.CreateTicketRequest;
import com.example.ticket.api.dto.PatchTicketRequest;
import com.example.ticket.api.dto.TicketFilter;
import com.example.ticket.api.dto.TicketResponse;
import com.example.ticket.domain.Ticket;
import com.example.ticket.domain.TicketNotFoundException;
import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;
import com.example.rag.ingestion.TicketIngestionEventType;
import com.example.rag.ingestion.TicketIngestionPublisher;
import com.example.ticket.repository.TicketRepository;
import com.example.ticket.repository.TicketSpecifications;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private static final String TICKET_ID_PATTERN = "^TKT-[0-9]+$";

    private final TicketRepository ticketRepository;
    private final TicketIdGenerator ticketIdGenerator;
    private final TicketMapper ticketMapper;
    private final TicketIngestionPublisher ingestionPublisher;

    public TicketService(
            TicketRepository ticketRepository,
            TicketIdGenerator ticketIdGenerator,
            TicketMapper ticketMapper,
            TicketIngestionPublisher ingestionPublisher) {
        this.ticketRepository = ticketRepository;
        this.ticketIdGenerator = ticketIdGenerator;
        this.ticketMapper = ticketMapper;
        this.ingestionPublisher = ingestionPublisher;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request) {
        Instant now = Instant.now();
        Ticket ticket = new Ticket();
        ticket.setTicketId(ticketIdGenerator.nextTicketId());
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(
                request.priority() != null ? request.priority() : TicketPriority.MEDIUM);
        ticket.setAssignee(request.assignee());
        ticket.setCategory(request.category());
        ticket.setResolutionNotes(null);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        TicketResponse response = ticketMapper.toResponse(ticketRepository.save(ticket));
        ingestionPublisher.publish(response.ticketId(), TicketIngestionEventType.CREATE);
        return response;
    }

    @Transactional(readOnly = true)
    public TicketResponse getByTicketId(String ticketId) {
        validateTicketIdFormat(ticketId);
        Ticket ticket =
                ticketRepository
                        .findByTicketId(ticketId)
                        .orElseThrow(() -> new TicketNotFoundException(ticketId));
        return ticketMapper.toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> list(TicketFilter filter) {
        Specification<Ticket> spec =
                TicketSpecifications.combine(
                        filter.status(), filter.priority(), filter.assignee(), filter.category());
        return ticketRepository
                .findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Transactional
    public TicketResponse updateFields(String ticketId, PatchTicketRequest request) {
        validateTicketIdFormat(ticketId);
        Ticket ticket =
                ticketRepository
                        .findByTicketId(ticketId)
                        .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (request.title() != null) {
            ticket.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            ticket.setDescription(request.description());
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        if (request.assignee() != null) {
            ticket.setAssignee(request.assignee());
        }
        if (request.category() != null) {
            ticket.setCategory(request.category());
        }
        if (request.resolutionNotes() != null) {
            ticket.setResolutionNotes(request.resolutionNotes());
        }
        ticket.setUpdatedAt(Instant.now());
        TicketResponse response = ticketMapper.toResponse(ticketRepository.save(ticket));
        ingestionPublisher.publish(ticketId, TicketIngestionEventType.UPDATE);
        return response;
    }

    @Transactional(readOnly = true)
    public Ticket requireTicket(String ticketId) {
        validateTicketIdFormat(ticketId);
        return ticketRepository
                .findByTicketId(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    private static void validateTicketIdFormat(String ticketId) {
        if (ticketId == null || !ticketId.matches(TICKET_ID_PATTERN)) {
            throw new TicketNotFoundException(ticketId);
        }
    }
}