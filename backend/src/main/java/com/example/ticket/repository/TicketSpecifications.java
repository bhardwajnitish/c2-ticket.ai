package com.example.ticket.repository;

import com.example.ticket.domain.Ticket;
import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

public final class TicketSpecifications {

    private TicketSpecifications() {}

    public static Specification<Ticket> withStatus(TicketStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Ticket> withPriority(TicketPriority priority) {
        return (root, query, cb) -> cb.equal(root.get("priority"), priority);
    }

    public static Specification<Ticket> withAssignee(String assignee) {
        return (root, query, cb) -> cb.equal(root.get("assignee"), assignee);
    }

    public static Specification<Ticket> withCategory(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<Ticket> combine(
            TicketStatus status,
            TicketPriority priority,
            String assignee,
            String category) {
        Specification<Ticket> spec = (root, query, cb) -> null;
        if (status != null) {
            spec = spec.and(withStatus(status));
        }
        if (priority != null) {
            spec = spec.and(withPriority(priority));
        }
        if (assignee != null) {
            spec = spec.and(withAssignee(assignee));
        }
        if (category != null) {
            spec = spec.and(withCategory(category));
        }
        return spec;
    }
}