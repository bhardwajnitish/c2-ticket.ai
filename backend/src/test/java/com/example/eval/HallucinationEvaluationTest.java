package com.example.eval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.rag.api.RagQueryOutcome;
import com.example.rag.api.RagResponseMapper;
import com.example.rag.generation.GeneratorClient;
import com.example.rag.query.RagQueryService;
import com.example.rag.retrieval.RagRetrievalService;
import com.example.rag.retrieval.ScoredChunk;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class HallucinationEvaluationTest {

    @Autowired private RagQueryService ragQueryService;

    @MockBean private RagRetrievalService ragRetrievalService;

    @MockBean private GeneratorClient generatorClient;

    @Test
    void thirtyMinuteContextWithTwentyFourHourAnswerDowngraded() throws Exception {
        GoldenDatasetCase goldenCase =
                EvalResourceSupport.loadGoldenDataset().cases().stream()
                        .filter(c -> "hallucination-duration-expiry".equals(c.id()))
                        .findFirst()
                        .orElseThrow();

        List<ScoredChunk> keepSet =
                goldenCase.contextChunks().stream()
                        .map(
                                chunk ->
                                        new ScoredChunk(
                                                chunk.ticketId(), 0, chunk.content(), 0.91))
                        .toList();

        when(ragRetrievalService.retrieve(goldenCase.query())).thenReturn(keepSet);
        when(generatorClient.generate(anyString())).thenReturn(goldenCase.generatorOutput());

        var response = ragQueryService.query(goldenCase.query());

        assertThat(response.outcome()).isEqualTo(RagQueryOutcome.NO_MATCH);
        assertThat(response.answer()).isNull();
        assertThat(response.noMatch().code()).isEqualTo(RagResponseMapper.GROUNDING_GATE_FAILED);
        assertThat(response.noMatch().code()).isEqualTo(goldenCase.expectNoMatchCode());
        assertThat(response.citations()).isEmpty();
        assertThat(response.escalation().recommended()).isTrue();
    }

    @Test
    void conflictingContextFixtureFailsGateWithoutApiAnswer() {
        var gate =
                new com.example.rag.grounding.RagGroundingGate(
                        new com.example.rag.grounding.CitationValidator(),
                        new com.example.rag.grounding.RagAnswerGroundingReviewer());

        boolean passes =
                gate.passes(
                        ConflictingContextFixture.HALLUCINATED_ANSWER,
                        java.util.Set.of("TKT-1001"),
                        ConflictingContextFixture.CONTEXT_CHUNKS);

        assertThat(passes).isFalse();
    }
}