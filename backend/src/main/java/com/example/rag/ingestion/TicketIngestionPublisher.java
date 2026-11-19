package com.example.rag.ingestion;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class TicketIngestionPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public TicketIngestionPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publish(String ticketId, TicketIngestionEventType type) {
        eventPublisher.publishEvent(new TicketIngestionEvent(ticketId, type));
    }
}