package com.example.rag.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ticket.api.dto.CreateTicketRequest;
import com.example.ticket.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class TicketIngestionIntegrationTest {

    @Autowired private TransactionTemplate transactionTemplate;

    @Autowired private TicketService ticketService;

    @Autowired private InMemoryTicketVectorStore vectorStore;

    @Test
    void ingestsAfterCommittedCreate() {
        String ticketId =
                transactionTemplate.execute(
                        status ->
                                ticketService
                                        .create(new CreateTicketRequest("Vector title", "Body", null, null, null))
                                        .ticketId());

        assertThat(vectorStore.chunksForTicket(ticketId)).isNotEmpty();
        assertThat(vectorStore.chunksForTicket(ticketId).get(0).content())
                .contains("[Ticket " + ticketId + "]");
    }

    @Test
    void inMemorySearchReturnsNeighbors() {
        String ticketId =
                transactionTemplate.execute(
                        status ->
                                ticketService
                                        .create(new CreateTicketRequest("Searchable", "password reset", null, null, null))
                                        .ticketId());

        TestEmbeddingClient client = new TestEmbeddingClient(
                new com.example.rag.config.RagEmbeddingProperties("test", "nomic-embed-text", 768, "http://x"));
        float[] query = client.embed(java.util.List.of("password reset")).get(0);
        var neighbors = vectorStore.searchByCosineDistance(query, 5);

        assertThat(neighbors).isNotEmpty();
        assertThat(neighbors.get(0).ticketId()).isEqualTo(ticketId);
    }
}