package com.example.ticket.api.dto;

import java.time.Instant;

public record CommentResponse(
        String id, String ticketId, String author, String content, Instant createdAt) {}