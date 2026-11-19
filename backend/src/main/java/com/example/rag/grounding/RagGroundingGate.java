package com.example.rag.grounding;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RagGroundingGate {

    private final CitationValidator citationValidator;
    private final RagAnswerGroundingReviewer durationReviewer;

    public RagGroundingGate(
            CitationValidator citationValidator, RagAnswerGroundingReviewer durationReviewer) {
        this.citationValidator = citationValidator;
        this.durationReviewer = durationReviewer;
    }

  public boolean passes(String answerText, Set<String> keepSetTicketIds, List<ContextChunk> contextChunks) {
        if (!citationValidator.allTicketIdsInKeepSet(answerText, keepSetTicketIds)) {
            return false;
        }
        return !durationReviewer.hasDurationContradiction(answerText, contextChunks);
    }
}