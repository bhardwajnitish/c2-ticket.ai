package com.example.ticket.api.dto;

import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;

public record TicketFilter(
        TicketStatus status, TicketPriority priority, String assignee, String category) {}