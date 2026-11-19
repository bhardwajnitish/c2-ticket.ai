package com.example.ticket.domain;

public class TicketNotFoundException extends RuntimeException {

    private final String ticketId;

    public TicketNotFoundException(String ticketId) {
        super("Ticket not found: " + ticketId);
        this.ticketId = ticketId;
    }

    public String ticketId() {
        return ticketId;
    }
}