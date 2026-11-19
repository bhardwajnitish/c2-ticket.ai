package com.example.eval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.rag.api.RagQueryOutcome;
import com.example.rag.api.dto.RagCitationDto;
import com.example.rag.generation.GeneratorClient;
import com.example.rag.query.RagQueryService;
import com.example.rag.retrieval.RagRetrievalService;
import com.example.rag.retrieval.ScoredChunk;
import com.example.ticket.domain.Ticket;
import com.example.ticket.repository.TicketRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GroundingEvaluationTest {

    @Autowired private RagQueryService ragQueryService;

    @MockBean private RagRetrievalService ragRetrievalService;

    @MockBean private GeneratorClient generatorClient;

    @MockBean private TicketRepository ticketRepository;

    @Test
    void supportedAnswerPassesGateFromGoldenCase() throws Exception {
        GoldenDatasetCase goldenCase =
                EvalResourceSupport.loadGoldenDataset().cases().stream()
                        .filter(c -> "grounding-supported-answer".equals(c.id()))
                        .findFirst()
                        .orElseThrow();

        List<ScoredChunk> keepSet =
                goldenCase.contextChunks().stream()
                        .map(
                                chunk ->
                                        new ScoredChunk(
                                                chunk.ticketId(), 0, chunk.content(), 0.92))
                        .toList();

        when(ragRetrievalService.retrieve(goldenCase.query())).thenReturn(keepSet);
        when(generatorClient.generate(anyString())).thenReturn(goldenCase.generatorOutput());

        Ticket ticket = new Ticket();
        ticket.setTicketId("TKT-1001");
        ticket.setTitle("Password reset email missing");
        when(ticketRepository.findByTicketIdIn(List.of("TKT-1001"))).thenReturn(List.of(ticket));

        var response = ragQueryService.query(goldenCase.query());

        assertThat(response.outcome()).isEqualTo(RagQueryOutcome.ANSWER);
        assertThat(response.answer()).isNotNull();
        assertThat(response.answer().text()).isEqualTo(goldenCase.generatorOutput());
        assertThat(response.citations())
                .extracting(RagCitationDto::ticketId)
                .containsExactlyElementsOf(goldenCase.expectCitationTicketIds());
    }
}