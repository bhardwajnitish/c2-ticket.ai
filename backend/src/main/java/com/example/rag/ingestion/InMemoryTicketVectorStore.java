package com.example.rag.ingestion;

import com.example.rag.retrieval.VectorNeighbor;
import com.example.rag.retrieval.VectorSearchPort;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("test")
public class InMemoryTicketVectorStore implements TicketVectorStore, VectorSearchPort {

    private final Map<String, List<TicketChunkRecord>> byTicket = new ConcurrentHashMap<>();

    @Override
    public List<ChunkFingerprintRow> findFingerprintsByTicketId(String ticketId) {
        return byTicket.getOrDefault(ticketId, List.of()).stream()
                .sorted(Comparator.comparingInt(TicketChunkRecord::chunkIndex))
                .map(c -> new ChunkFingerprintRow(c.chunkIndex(), c.contentFingerprint()))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void replaceChunks(String ticketId, List<TicketChunkRecord> chunks) {
        byTicket.put(ticketId, List.copyOf(chunks));
    }

    @Override
    public synchronized void deleteByTicketId(String ticketId) {
        byTicket.remove(ticketId);
    }

    @Override
    public List<VectorNeighbor> searchByCosineDistance(float[] queryEmbedding, int limit) {
        List<VectorNeighbor> neighbors = new ArrayList<>();
        for (List<TicketChunkRecord> chunks : byTicket.values()) {
            for (TicketChunkRecord chunk : chunks) {
                double distance = cosineDistance(queryEmbedding, chunk.embedding());
                neighbors.add(
                        new VectorNeighbor(
                                chunk.ticketId(), chunk.chunkIndex(), chunk.content(), distance));
            }
        }
        neighbors.sort(Comparator.comparingDouble(VectorNeighbor::cosineDistance));
        return neighbors.stream().limit(limit).toList();
    }

    public List<TicketChunkRecord> chunksForTicket(String ticketId) {
        return byTicket.getOrDefault(ticketId, List.of());
    }

    public synchronized void clearAll() {
        byTicket.clear();
    }

    static double cosineDistance(float[] a, float[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 1.0;
        }
        double similarity = dot / (Math.sqrt(normA) * Math.sqrt(normB));
        return 1.0 - similarity;
    }
}