package com.example.rag.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.rag.config.RagRetrievalProperties;
import com.example.rag.ingestion.EmbeddingClient;
import java.util.List;
import org.junit.jupiter.api.Test;

class RagRetrievalServiceTest {

    private static final VectorSearchPort NO_SEARCH = (e, l) -> List.of();
    private static final EmbeddingClient NO_EMBED =
            new EmbeddingClient() {
                @Override
                public List<float[]> embed(List<String> texts) {
                    return List.of();
                }

                @Override
                public String modelName() {
                    return "test";
                }
            };

    @Test
    void appliesTopKThenThresholdFromProperties() {
        RagRetrievalProperties properties = new RagRetrievalProperties(2, 0.8);
        RagRetrievalService service = new RagRetrievalService(properties, NO_SEARCH, NO_EMBED);

        List<ScoredChunk> candidates =
                List.of(
                        new ScoredChunk("TKT-1001", 0, "a", 0.9),
                        new ScoredChunk("TKT-1002", 0, "b", 0.75),
                        new ScoredChunk("TKT-1003", 0, "c", 0.95));

        List<ScoredChunk> keepSet = service.applyTopKThenThreshold(candidates);

        assertThat(keepSet).extracting(ScoredChunk::ticketId).containsExactly("TKT-1003", "TKT-1001");
    }

    @Test
    void excludesCandidatesBelowThresholdAfterTopK() {
        RagRetrievalProperties properties = new RagRetrievalProperties(3, 0.85);
        RagRetrievalService service = new RagRetrievalService(properties, NO_SEARCH, NO_EMBED);

        List<ScoredChunk> candidates =
                List.of(
                        new ScoredChunk("TKT-1001", 0, "a", 0.9),
                        new ScoredChunk("TKT-1002", 0, "b", 0.84),
                        new ScoredChunk("TKT-1003", 0, "c", 0.88));

        List<ScoredChunk> keepSet = service.applyTopKThenThreshold(candidates);

        assertThat(keepSet).extracting(ScoredChunk::ticketId).containsExactly("TKT-1001", "TKT-1003");
    }
}