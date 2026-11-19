package com.example.rag.grounding;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.rag.support.PasswordResetKnowledgeFixture;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RagGroundingGateTest {

    private final RagGroundingGate gate =
            new RagGroundingGate(new CitationValidator(), new RagAnswerGroundingReviewer());

    @Test
    void rejectsHallucinatedDurationAnswer() {
        assertThat(
                        gate.passes(
                                PasswordResetKnowledgeFixture.HALLUCINATED_ANSWER,
                                Set.of("TKT-1001"),
                                PasswordResetKnowledgeFixture.CONTEXT_CHUNKS))
                .isFalse();
    }

    @Test
    void rejectsForeignTicketId() {
        assertThat(
                        gate.passes(
                                "Resolved in TKT-9999.",
                                Set.of("TKT-1001"),
                                PasswordResetKnowledgeFixture.CONTEXT_CHUNKS))
                .isFalse();
    }
}