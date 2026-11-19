package com.example.ticket.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TicketStatusTransitionPolicyTest {

    @ParameterizedTest
    @MethodSource("legalTransitions")
    void allowsLegalTransitions(TicketStatus from, TicketStatus to) {
        assertThat(TicketStatusTransitionPolicy.evaluate(from, to))
                .isEqualTo(TicketStatusTransitionPolicy.Decision.ALLOWED);
    }

    @ParameterizedTest
    @MethodSource("illegalTransitions")
    void rejectsIllegalTransitions(TicketStatus from, TicketStatus to) {
        assertThat(TicketStatusTransitionPolicy.evaluate(from, to))
                .isEqualTo(TicketStatusTransitionPolicy.Decision.REJECTED);
    }

    static Stream<Arguments> legalTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CLOSED));
    }

    static Stream<Arguments> illegalTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.RESOLVED));
    }
}