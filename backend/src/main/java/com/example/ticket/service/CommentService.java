package com.example.ticket.service;

import com.example.ticket.api.dto.CommentResponse;
import com.example.ticket.api.dto.CreateCommentRequest;
import com.example.ticket.domain.Comment;
import com.example.ticket.domain.Ticket;
import com.example.rag.ingestion.TicketIngestionEventType;
import com.example.rag.ingestion.TicketIngestionPublisher;
import com.example.ticket.repository.CommentRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketService ticketService;
    private final TicketMapper ticketMapper;
    private final TicketIngestionPublisher ingestionPublisher;

    public CommentService(
            CommentRepository commentRepository,
            TicketService ticketService,
            TicketMapper ticketMapper,
            TicketIngestionPublisher ingestionPublisher) {
        this.commentRepository = commentRepository;
        this.ticketService = ticketService;
        this.ticketMapper = ticketMapper;
        this.ingestionPublisher = ingestionPublisher;
    }

    @Transactional
    public CommentResponse addComment(String ticketId, CreateCommentRequest request) {
        Ticket ticket = ticketService.requireTicket(ticketId);
        Instant now = Instant.now();
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setTicketId(ticket.getTicketId());
        comment.setAuthor(request.author());
        comment.setContent(request.content().trim());
        comment.setCreatedAt(now);
        commentRepository.save(comment);
        CommentResponse response = ticketMapper.toCommentResponse(comment);
        ingestionPublisher.publish(ticketId, TicketIngestionEventType.COMMENT);
        return response;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> listComments(String ticketId) {
        ticketService.requireTicket(ticketId);
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(ticketMapper::toCommentResponse)
                .toList();
    }
}