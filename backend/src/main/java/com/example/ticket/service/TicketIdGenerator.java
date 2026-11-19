package com.example.ticket.service;

import com.example.ticket.domain.TicketIdSequence;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketIdGenerator {

    private final JdbcTemplate jdbcTemplate;

    public TicketIdGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public String nextTicketId() {
        Long suffix =
                jdbcTemplate.queryForObject(
                        "SELECT nextval('ticket_id_seq')", Long.class);
        return TicketIdSequence.fromSuffix(suffix).ticketId();
    }
}