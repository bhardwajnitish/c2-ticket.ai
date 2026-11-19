package com.example.rag.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.rag.ingestion.InMemoryTicketVectorStore;
import org.junit.jupiter.api.Test;

class TestEmbeddingClientTest {

    @Test
    void unrelatedTextsProduceDifferentVectors() {
        float[] a = TestEmbeddingClient.deterministicVector("alpha", 768);
        float[] b = TestEmbeddingClient.deterministicVector("beta", 768);
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void unrelatedTextsHaveLowSimilarity() {
        float[] password =
                TestEmbeddingClient.deterministicVector("password reset email missing", 768);
        float[] billing =
                TestEmbeddingClient.deterministicVector("billing invoice PDF for March", 768);
        double distance = InMemoryTicketVectorStore.cosineDistance(password, billing);
        assertThat(1.0 - distance).isLessThan(0.72);
    }

    @Test
    void relatedTextsShareHigherSimilarity() {
        float[] chunk =
                TestEmbeddingClient.deterministicVector(
                        """
                        [Ticket TKT-1001] Password reset email missing
                        Password reset email missing
                        User does not receive reset email""",
                        768);
        float[] query = TestEmbeddingClient.deterministicVector("password reset not working", 768);
        double similarity = 1.0 - InMemoryTicketVectorStore.cosineDistance(chunk, query);
        assertThat(similarity).isGreaterThanOrEqualTo(0.72);
    }
}