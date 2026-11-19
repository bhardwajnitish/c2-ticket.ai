package com.example.rag.retrieval;

import com.example.rag.config.RagRetrievalProperties;
import com.example.rag.ingestion.EmbeddingClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Applies retrieval policy: top-K by similarity first, then similarity-threshold filter.
 */
@Service
public class RagRetrievalService {

    private final RagRetrievalProperties retrievalProperties;
    private final VectorSearchPort vectorSearchPort;
    private final EmbeddingClient embeddingClient;

    public RagRetrievalService(
            RagRetrievalProperties retrievalProperties,
            VectorSearchPort vectorSearchPort,
            EmbeddingClient embeddingClient) {
        this.retrievalProperties = retrievalProperties;
        this.vectorSearchPort = vectorSearchPort;
        this.embeddingClient = embeddingClient;
    }

    public List<ScoredChunk> retrieve(String query) {
        List<float[]> vectors = embeddingClient.embed(List.of(query));
        if (vectors.isEmpty()) {
            return List.of();
        }
        int topK = retrievalProperties.topK();
        List<VectorNeighbor> neighbors;
        try {
            neighbors = vectorSearchPort.searchByCosineDistance(vectors.get(0), topK);
        } catch (RuntimeException ex) {
            if (ex instanceof VectorStoreException) {
                throw ex;
            }
            throw new VectorStoreException("Vector search failed", ex);
        }
        List<ScoredChunk> candidates =
                neighbors.stream()
                        .map(
                                n ->
                                        new ScoredChunk(
                                                n.ticketId(),
                                                n.chunkIndex(),
                                                n.content(),
                                                1.0 - n.cosineDistance()))
                        .toList();
        return applyTopKThenThreshold(candidates);
    }

    /**
     * @param candidates similarity search results (any order); sorted internally descending
     * @return keep-set chunks surviving top-K then threshold
     */
    public List<ScoredChunk> applyTopKThenThreshold(List<ScoredChunk> candidates) {
        int topK = retrievalProperties.topK();
        double threshold = retrievalProperties.similarityThreshold();

        List<ScoredChunk> ranked = candidates.stream()
                .sorted(Comparator.comparingDouble(ScoredChunk::similarity).reversed())
                .limit(topK)
                .toList();

        List<ScoredChunk> keepSet = new ArrayList<>();
        for (ScoredChunk chunk : ranked) {
            if (chunk.similarity() >= threshold) {
                keepSet.add(chunk);
            }
        }
        return List.copyOf(keepSet);
    }
}