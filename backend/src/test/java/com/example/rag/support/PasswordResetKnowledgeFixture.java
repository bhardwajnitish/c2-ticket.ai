package com.example.rag.support;

import com.example.rag.grounding.ContextChunk;
import com.example.rag.retrieval.ScoredChunk;
import java.util.List;

public final class PasswordResetKnowledgeFixture {

    public static final String QUERY = "How long do password reset links last?";

    public static final String CONTEXT_CHUNK_CONTENT =
            "[Ticket TKT-1001] Password reset email missing\n"
                    + "Password reset links expire after 30 minutes.";

    public static final String HALLUCINATED_ANSWER = "Password reset links expire after 24 hours.";

    public static final List<ScoredChunk> KEEP_SET =
            List.of(new ScoredChunk("TKT-1001", 0, CONTEXT_CHUNK_CONTENT, 0.91));

    public static final List<ContextChunk> CONTEXT_CHUNKS =
            List.of(new ContextChunk("TKT-1001", CONTEXT_CHUNK_CONTENT));

    private PasswordResetKnowledgeFixture() {}
}