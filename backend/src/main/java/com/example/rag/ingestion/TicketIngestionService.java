package com.example.rag.ingestion;

import com.example.rag.config.RagChunkProperties;
import com.example.rag.config.RagEmbeddingProperties;
import com.example.ticket.domain.Ticket;
import com.example.ticket.domain.TicketNotFoundException;
import com.example.ticket.repository.CommentRepository;
import com.example.ticket.repository.TicketRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TicketIngestionService {

    private static final Logger log = LoggerFactory.getLogger(TicketIngestionService.class);

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final KnowledgeDocumentBuilder knowledgeDocumentBuilder;
    private final SectionChunker sectionChunker;
    private final EmbeddingClient embeddingClient;
    private final TicketVectorStore ticketVectorStore;
    private final RagChunkProperties chunkProperties;
    private final RagEmbeddingProperties embeddingProperties;

    public TicketIngestionService(
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            KnowledgeDocumentBuilder knowledgeDocumentBuilder,
            SectionChunker sectionChunker,
            EmbeddingClient embeddingClient,
            TicketVectorStore ticketVectorStore,
            RagChunkProperties chunkProperties,
            RagEmbeddingProperties embeddingProperties) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.knowledgeDocumentBuilder = knowledgeDocumentBuilder;
        this.sectionChunker = sectionChunker;
        this.embeddingClient = embeddingClient;
        this.ticketVectorStore = ticketVectorStore;
        this.chunkProperties = chunkProperties;
        this.embeddingProperties = embeddingProperties;
    }

    public void ingest(String ticketId) {
        Ticket ticket =
                ticketRepository
                        .findByTicketId(ticketId)
                        .orElseThrow(() -> new TicketNotFoundException(ticketId));

        var comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);
        var document = knowledgeDocumentBuilder.build(ticket, comments);
        List<String> bodies = sectionChunker.chunkBodies(ticket, document);

        List<ChunkPlan> plans = new ArrayList<>();
        for (int i = 0; i < bodies.size(); i++) {
            String content = sectionChunker.toChunkContent(ticket, bodies.get(i));
            String fingerprint =
                    ContentFingerprint.compute(
                            content,
                            ticket,
                            chunkProperties.version(),
                            embeddingClient.modelName());
            plans.add(new ChunkPlan(i, content, fingerprint));
        }

        if (fingerprintsUnchanged(ticketId, plans)) {
            return;
        }

        if (plans.isEmpty()) {
            ticketVectorStore.deleteByTicketId(ticketId);
            return;
        }

        List<String> texts = plans.stream().map(ChunkPlan::content).toList();
        List<float[]> embeddings;
        try {
            embeddings = embeddingClient.embed(texts);
        } catch (EmbeddingException ex) {
            log.error("Embedding failed for ticket {}: {}", ticketId, ex.getMessage(), ex);
            return;
        }

        if (embeddings.size() != plans.size()) {
            log.error(
                    "Embedding size mismatch for ticket {}: expected {}, got {}",
                    ticketId,
                    plans.size(),
                    embeddings.size());
            return;
        }

        int expectedDim = embeddingProperties.dimension();
        Instant ingestedAt = Instant.now();
        List<TicketChunkRecord> records = new ArrayList<>();
        for (int i = 0; i < plans.size(); i++) {
            float[] vector = embeddings.get(i);
            if (vector.length != expectedDim) {
                log.error(
                        "Embedding dimension mismatch for ticket {} chunk {}: expected {}, got {}",
                        ticketId,
                        i,
                        expectedDim,
                        vector.length);
                return;
            }
            ChunkPlan plan = plans.get(i);
            records.add(
                    new TicketChunkRecord(
                            ticket.getTicketId(),
                            plan.chunkIndex(),
                            plan.content(),
                            ticket.getStatus(),
                            ticket.getPriority(),
                            ticket.getAssignee(),
                            ticket.getCategory(),
                            plan.fingerprint(),
                            embeddingClient.modelName(),
                            chunkProperties.version(),
                            ingestedAt,
                            vector));
        }

        ticketVectorStore.replaceChunks(ticketId, records);
    }

    private boolean fingerprintsUnchanged(String ticketId, List<ChunkPlan> plans) {
        List<ChunkFingerprintRow> existing =
                ticketVectorStore.findFingerprintsByTicketId(ticketId);
        if (existing.size() != plans.size()) {
            return false;
        }
        for (int i = 0; i < plans.size(); i++) {
            ChunkPlan plan = plans.get(i);
            ChunkFingerprintRow row = existing.get(i);
            if (plan.chunkIndex() != row.chunkIndex()
                    || !plan.fingerprint().equals(row.contentFingerprint())) {
                return false;
            }
        }
        return !plans.isEmpty();
    }

    private record ChunkPlan(int chunkIndex, String content, String fingerprint) {}
}