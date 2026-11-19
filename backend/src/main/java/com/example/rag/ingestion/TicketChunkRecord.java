package com.example.rag.ingestion;

import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;
import java.time.Instant;

public record TicketChunkRecord(
        String ticketId,
        int chunkIndex,
        String content,
        TicketStatus status,
        TicketPriority priority,
        String assignee,
        String category,
        String contentFingerprint,
        String embeddingModel,
        String chunkerVersion,
        Instant ingestedAt,
        float[] embedding) {}