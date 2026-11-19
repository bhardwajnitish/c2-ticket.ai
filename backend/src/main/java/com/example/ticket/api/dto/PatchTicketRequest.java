package com.example.ticket.api.dto;

import com.example.ticket.domain.TicketPriority;
import jakarta.validation.constraints.Size;

public record PatchTicketRequest(
        @Size(min = 1, max = 255) String title,
        @Size(max = 10000) String description,
        TicketPriority priority,
        @Size(max = 255) String assignee,
        @Size(max = 255) String category,
        @Size(max = 10000) String resolutionNotes,
        com.example.ticket.domain.TicketStatus status) {

    public boolean hasAnyAllowedField() {
        return title != null
                || description != null
                || priority != null
                || assignee != null
                || category != null
                || resolutionNotes != null;
    }
}