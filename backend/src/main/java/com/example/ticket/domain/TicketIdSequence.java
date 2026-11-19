package com.example.ticket.domain;

public record TicketIdSequence(long suffix) {

    private static final String PREFIX = "TKT-";

    public String ticketId() {
        return PREFIX + suffix;
    }

    public static TicketIdSequence fromSuffix(long suffix) {
        return new TicketIdSequence(suffix);
    }
}