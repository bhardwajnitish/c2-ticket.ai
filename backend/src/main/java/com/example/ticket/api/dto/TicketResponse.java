package com.example.ticket.api.dto;

import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record TicketResponse(
        String ticketId,
        String title,
        String description,
        TicketPriority priority,
        TicketStatus status,
        String assignee,
        String category,
        String resolutionNotes,
        Instant createdAt,
        Instant updatedAt) {}