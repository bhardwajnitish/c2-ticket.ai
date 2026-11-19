package com.example.rag.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.rag.config.RagChunkProperties;
import com.example.rag.config.RagEmbeddingProperties;
import com.example.ticket.domain.Ticket;
import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;
import com.example.ticket.repository.CommentRepository;
import com.example.ticket.repository.TicketRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketIngestionServiceTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private EmbeddingClient embeddingClient;

    private InMemoryTicketVectorStore vectorStore;
    private TicketIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        vectorStore = new InMemoryTicketVectorStore();
        ingestionService =
                new TicketIngestionService(
                        ticketRepository,
                        commentRepository,
                        new KnowledgeDocumentBuilder(),
                        new SectionChunker(new RagChunkProperties(2000, 250, 512, 64, "section-v1")),
                        embeddingClient,
                        vectorStore,
                        new RagChunkProperties(2000, 250, 512, 64, "section-v1"),
                        new RagEmbeddingProperties("test", "nomic-embed-text", 768, "http://localhost"));
    }

    @Test
    void replaceChunksOnSuccessfulIngest() {
        Ticket ticket = sampleTicket();
        when(ticketRepository.findByTicketId("TKT-1001")).thenReturn(Optional.of(ticket));
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc("TKT-1001")).thenReturn(List.of());
        when(embeddingClient.modelName()).thenReturn("nomic-embed-text");
        when(embeddingClient.embed(anyList())).thenReturn(List.of(TestEmbeddingClient.deterministicVector("body", 768)));

        ingestionService.ingest("TKT-1001");

        assertThat(vectorStore.chunksForTicket("TKT-1001")).hasSize(1);
    }

    @Test
    void deletesChunksWhenDocumentEmpty() {
        Ticket ticket = sampleTicket();
        ticket.setTitle("   ");
        ticket.setDescription(null);
        when(ticketRepository.findByTicketId("TKT-1001")).thenReturn(Optional.of(ticket));
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc("TKT-1001")).thenReturn(List.of());
        vectorStore.replaceChunks(
                "TKT-1001",
                List.of(
                        new TicketChunkRecord(
                                "TKT-1001",
                                0,
                                "old",
                                TicketStatus.OPEN,
                                TicketPriority.MEDIUM,
                                null,
                                null,
                                "fp",
                                "m",
                                "section-v1",
                                java.time.Instant.now(),
                                TestEmbeddingClient.deterministicVector("old", 768))));

        ingestionService.ingest("TKT-1001");

        assertThat(vectorStore.chunksForTicket("TKT-1001")).isEmpty();
        verify(embeddingClient, never()).embed(anyList());
    }

    @Test
    void embeddingFailureLeavesOldVectors() {
        Ticket ticket = sampleTicket();
        when(ticketRepository.findByTicketId("TKT-1001")).thenReturn(Optional.of(ticket));
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc("TKT-1001")).thenReturn(List.of());
        when(embeddingClient.modelName()).thenReturn("nomic-embed-text");
        when(embeddingClient.embed(anyList())).thenThrow(new EmbeddingException("fail", new RuntimeException("down")));

        vectorStore.replaceChunks(
                "TKT-1001",
                List.of(
                        new TicketChunkRecord(
                                "TKT-1001",
                                0,
                                "old",
                                TicketStatus.OPEN,
                                TicketPriority.MEDIUM,
                                null,
                                null,
                                "fp",
                                "m",
                                "section-v1",
                                java.time.Instant.now(),
                                TestEmbeddingClient.deterministicVector("old", 768))));

        ingestionService.ingest("TKT-1001");

        assertThat(vectorStore.chunksForTicket("TKT-1001")).hasSize(1);
        assertThat(vectorStore.chunksForTicket("TKT-1001").get(0).content()).isEqualTo("old");
    }

    private static Ticket sampleTicket() {
        Ticket ticket = new Ticket();
        ticket.setTicketId("TKT-1001");
        ticket.setTitle("Reset email");
        ticket.setDescription("User cannot receive reset mail");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        return ticket;
    }
}