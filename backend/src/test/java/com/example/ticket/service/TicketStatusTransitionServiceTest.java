package com.example.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ticket.api.dto.CreateTicketRequest;
import com.example.ticket.domain.InvalidStatusTransitionException;
import com.example.ticket.domain.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketStatusTransitionServiceTest {

    @Autowired private TicketService ticketService;

    @Autowired private TicketStatusTransitionService transitionService;

    @Test
    void legalTransitionUpdatesStatus() {
        var created =
                ticketService.create(new CreateTicketRequest("Title", null, null, null, null));
        var updated =
                transitionService.transition(created.ticketId(), TicketStatus.IN_PROGRESS);
        assertThat(updated.status()).isEqualTo(TicketStatus.IN_PROGRESS);
    }

    @Test
    void illegalTransitionLeavesStatusUnchanged() {
        var created =
                ticketService.create(new CreateTicketRequest("Title", null, null, null, null));
        assertThatThrownBy(
                        () ->
                                transitionService.transition(
                                        created.ticketId(), TicketStatus.RESOLVED))
                .isInstanceOf(InvalidStatusTransitionException.class);

        var reloaded = ticketService.getByTicketId(created.ticketId());
        assertThat(reloaded.status()).isEqualTo(TicketStatus.OPEN);
    }

    @Test
    void sameStateRejected() {
        var created =
                ticketService.create(new CreateTicketRequest("Title", null, null, null, null));
        assertThatThrownBy(
                        () -> transitionService.transition(created.ticketId(), TicketStatus.OPEN))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }
}