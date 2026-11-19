package com.example.ticket.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TicketStatusTransitionPolicy {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED =
            buildAllowed();

    public enum Decision {
        ALLOWED,
        REJECTED
    }

    private TicketStatusTransitionPolicy() {}

    public static Decision evaluate(TicketStatus from, TicketStatus to) {
        if (from == to) {
            return Decision.REJECTED;
        }
        if (from == TicketStatus.CLOSED || from == TicketStatus.CANCELLED) {
            return Decision.REJECTED;
        }
        Set<TicketStatus> targets = ALLOWED.get(from);
        if (targets == null || !targets.contains(to)) {
            return Decision.REJECTED;
        }
        return Decision.ALLOWED;
    }

    private static Map<TicketStatus, Set<TicketStatus>> buildAllowed() {
        Map<TicketStatus, Set<TicketStatus>> map = new EnumMap<>(TicketStatus.class);
        map.put(TicketStatus.OPEN, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED));
        map.put(
                TicketStatus.IN_PROGRESS,
                EnumSet.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED));
        map.put(TicketStatus.RESOLVED, EnumSet.of(TicketStatus.CLOSED));
        map.put(TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class));
        map.put(TicketStatus.CANCELLED, EnumSet.noneOf(TicketStatus.class));
        return Map.copyOf(map);
    }
}