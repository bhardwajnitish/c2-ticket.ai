package com.example.ticket.api.dto;

import com.example.ticket.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 10000) String description,
        TicketPriority priority,
        @Size(max = 255) String assignee,
        @Size(max = 255) String category) {}