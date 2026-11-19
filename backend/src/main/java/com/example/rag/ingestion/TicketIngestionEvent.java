package com.example.rag.ingestion;

public record TicketIngestionEvent(String ticketId, TicketIngestionEventType type) {}