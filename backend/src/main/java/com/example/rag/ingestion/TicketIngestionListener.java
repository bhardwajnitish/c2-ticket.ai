package com.example.rag.ingestion;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TicketIngestionListener {

    private final TicketIngestionService ticketIngestionService;

    public TicketIngestionListener(TicketIngestionService ticketIngestionService) {
        this.ticketIngestionService = ticketIngestionService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketChanged(TicketIngestionEvent event) {
        ticketIngestionService.ingest(event.ticketId());
    }
}