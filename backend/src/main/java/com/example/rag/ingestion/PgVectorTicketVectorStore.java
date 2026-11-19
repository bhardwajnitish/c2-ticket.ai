package com.example.rag.ingestion;

import com.example.rag.retrieval.VectorNeighbor;
import com.example.rag.retrieval.VectorSearchPort;
import com.example.rag.retrieval.VectorStoreException;
import com.pgvector.PGvector;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class PgVectorTicketVectorStore implements TicketVectorStore, VectorSearchPort {

    private final JdbcTemplate jdbcTemplate;

    public PgVectorTicketVectorStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ChunkFingerprintRow> findFingerprintsByTicketId(String ticketId) {
        return jdbcTemplate.query(
                """
                SELECT chunk_index, content_fingerprint
                FROM ticket_chunk
                WHERE ticket_id = ?
                ORDER BY chunk_index
                """,
                (rs, rowNum) ->
                        new ChunkFingerprintRow(
                                rs.getInt("chunk_index"), rs.getString("content_fingerprint")),
                ticketId);
    }

    @Override
    @Transactional
    public void replaceChunks(String ticketId, List<TicketChunkRecord> chunks) {
        jdbcTemplate.update("DELETE FROM ticket_chunk WHERE ticket_id = ?", ticketId);
        for (TicketChunkRecord chunk : chunks) {
            jdbcTemplate.update(
                    """
                    INSERT INTO ticket_chunk (
                      ticket_id, chunk_index, content, status, priority, assignee, category,
                      content_fingerprint, embedding_model, chunker_version, ingested_at, embedding
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    chunk.ticketId(),
                    chunk.chunkIndex(),
                    chunk.content(),
                    chunk.status().name(),
                    chunk.priority().name(),
                    chunk.assignee(),
                    chunk.category(),
                    chunk.contentFingerprint(),
                    chunk.embeddingModel(),
                    chunk.chunkerVersion(),
                    Timestamp.from(chunk.ingestedAt()),
                    new PGvector(chunk.embedding()));
        }
    }

    @Override
    @Transactional
    public void deleteByTicketId(String ticketId) {
        jdbcTemplate.update("DELETE FROM ticket_chunk WHERE ticket_id = ?", ticketId);
    }

    @Override
    public List<VectorNeighbor> searchByCosineDistance(float[] queryEmbedding, int limit) {
        try {
            return jdbcTemplate.query(
                    """
                    SELECT ticket_id, chunk_index, content, (embedding <=> ?) AS distance
                    FROM ticket_chunk
                    ORDER BY distance
                    LIMIT ?
                    """,
                    (rs, rowNum) -> mapNeighbor(rs),
                    new PGvector(queryEmbedding),
                    limit);
        } catch (RuntimeException ex) {
            throw new VectorStoreException("PGVector search failed", ex);
        }
    }

    private static VectorNeighbor mapNeighbor(ResultSet rs) throws SQLException {
        return new VectorNeighbor(
                rs.getString("ticket_id"),
                rs.getInt("chunk_index"),
                rs.getString("content"),
                rs.getDouble("distance"));
    }
}