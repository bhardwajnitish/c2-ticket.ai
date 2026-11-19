package com.example.rag.grounding;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.rag.support.PasswordResetKnowledgeFixture;
import org.junit.jupiter.api.Test;

class RagAnswerGroundingReviewerTest {

    private final RagAnswerGroundingReviewer reviewer = new RagAnswerGroundingReviewer();

    @Test
    void flags24HourAnswerAgainst30MinuteContext() {
        assertThat(
                        reviewer.hasDurationContradiction(
                                PasswordResetKnowledgeFixture.HALLUCINATED_ANSWER,
                                PasswordResetKnowledgeFixture.CONTEXT_CHUNKS))
                .isTrue();
    }

    @Test
    void accepts30MinuteAnswerMatchingContext() {
        assertThat(
                        reviewer.hasDurationContradiction(
                                "Password reset links expire after 30 minutes.",
                                PasswordResetKnowledgeFixture.CONTEXT_CHUNKS))
                .isFalse();
    }
}