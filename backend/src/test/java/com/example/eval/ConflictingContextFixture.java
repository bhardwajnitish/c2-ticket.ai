package com.example.eval;

import com.example.rag.grounding.ContextChunk;
import com.example.rag.retrieval.ScoredChunk;
import java.util.List;

public final class ConflictingContextFixture {

    public static final String HALLUCINATED_ANSWER =
            "Password reset links expire after 24 hours.";

    public static final List<ScoredChunk> KEEP_SET =
            List.of(
                    new ScoredChunk(
                            "TKT-1001",
                            0,
                            "[Ticket TKT-1001] Password policy\n"
                                    + "Password reset links expire after 30 minutes.",
                            0.9));

    public static final List<ContextChunk> CONTEXT_CHUNKS =
            List.of(
                    new ContextChunk(
                            "TKT-1001",
                            "[Ticket TKT-1001] Password policy\n"
                                    + "Password reset links expire after 30 minutes."));

    private ConflictingContextFixture() {}
}