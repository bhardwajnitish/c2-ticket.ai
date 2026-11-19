package com.example.eval;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ticket.api.dto.CreateTicketRequest;
import com.example.rag.retrieval.RagRetrievalService;
import com.example.rag.retrieval.ScoredChunk;
import com.example.ticket.service.TicketService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class RetrievalEvaluationTest {

    @Autowired private RagRetrievalService ragRetrievalService;

    @Autowired private TicketService ticketService;

    @Autowired private TransactionTemplate transactionTemplate;

    @Autowired private com.example.rag.ingestion.TicketIngestionService ticketIngestionService;

    @Autowired private com.example.rag.ingestion.InMemoryTicketVectorStore vectorStore;

    @BeforeEach
    void clearVectorStore() {
        vectorStore.clearAll();
    }

    @Test
    void retrievalExpectedTicketsGoldenCase() throws Exception {
        GoldenDatasetCase goldenCase =
                EvalResourceSupport.loadGoldenDataset().cases().stream()
                        .filter(c -> "retrieval-expected-tickets".equals(c.id()))
                        .findFirst()
                        .orElseThrow();

        Map<String, String> titleByTicketId =
                transactionTemplate.execute(
                        status -> {
                            Map<String, String> titles = new java.util.LinkedHashMap<>();
                            for (GoldenDatasetCase.SeedTicket seed : goldenCase.seedTickets()) {
                                var created =
                                        ticketService.create(
                                                new CreateTicketRequest(
                                                        seed.title(),
                                                        seed.description(),
                                                        null,
                                                        null,
                                                        null));
                                titles.put(created.ticketId(), created.title());
                                ticketIngestionService.ingest(created.ticketId());
                            }
                            return titles;
                        });

        List<ScoredChunk> keepSet = ragRetrievalService.retrieve(goldenCase.query());
        Set<String> keepTitles =
                keepSet.stream()
                        .map(chunk -> titleByTicketId.get(chunk.ticketId()))
                        .collect(Collectors.toSet());

        assertThat(keepTitles)
                .containsAll(goldenCase.expectKeepSetTitles());
        assertThat(keepTitles)
                .doesNotContainAnyElementsOf(goldenCase.expectExcludedTitles());
    }

    @Test
    void retrievalBelowThresholdGoldenCase() throws Exception {
        GoldenDatasetCase goldenCase =
                EvalResourceSupport.loadGoldenDataset().cases().stream()
                        .filter(c -> "retrieval-below-threshold".equals(c.id()))
                        .findFirst()
                        .orElseThrow();

        List<ScoredChunk> candidates =
                goldenCase.candidates().stream()
                        .map(
                                c ->
                                        new ScoredChunk(
                                                c.ticketId(), 0, "fixture", c.similarity()))
                        .toList();

        List<ScoredChunk> keepSet = ragRetrievalService.applyTopKThenThreshold(candidates);
        Set<String> keepIds =
                keepSet.stream().map(ScoredChunk::ticketId).collect(Collectors.toSet());

        assertThat(keepIds).containsExactlyElementsOf(goldenCase.expectKeepSetTicketIds());
        assertThat(keepIds)
                .doesNotContainAnyElementsOf(goldenCase.expectExcludedTicketIds());
    }
}