package com.example.ticket.service;

import com.example.ticket.api.dto.CommentResponse;
import com.example.ticket.api.dto.TicketResponse;
import com.example.ticket.domain.Comment;
import com.example.ticket.domain.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getAssignee(),
                ticket.getCategory(),
                ticket.getResolutionNotes(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt());
    }

    public CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId().toString(),
                comment.getTicketId(),
                comment.getAuthor(),
                comment.getContent(),
                comment.getCreatedAt());
    }
}