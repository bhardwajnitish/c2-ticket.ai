package com.example.ticket.domain;

public class InvalidStatusTransitionException extends RuntimeException {

    private final TicketStatus from;
    private final TicketStatus to;

    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Invalid status transition from " + from + " to " + to);
        this.from = from;
        this.to = to;
    }

    public TicketStatus from() {
        return from;
    }

    public TicketStatus to() {
        return to;
    }
}