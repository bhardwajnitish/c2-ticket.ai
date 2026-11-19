package com.example.eval;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.rag.ingestion.TicketIngestionService;
import com.example.rag.retrieval.RagRetrievalService;
import com.example.rag.retrieval.ScoredChunk;
import com.example.ticket.service.TicketService;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@EnabledIfEnvironmentVariable(named = "RUN_OLLAMA_CALIBRATION", matches = "true")
@SpringBootTest
@ActiveProfiles("ollama-calibration")
class OllamaRetrievalCalibrationEvaluation {

    @Autowired private RagRetrievalService ragRetrievalService;

    @Autowired private TicketService ticketService;

    @Autowired private TicketIngestionService ticketIngestionService;

    @Autowired private TransactionTemplate transactionTemplate;

    @Autowired private com.example.ticket.repository.TicketRepository ticketRepository;

    @Test
    void retrievalCalibrationQueriesScoreAgainstLiveEmbeddings() throws Exception {
        var manifest = EvalResourceSupport.loadRetrievalCalibrationQueries();

        for (RetrievalCalibrationCase calibrationCase : manifest.queries()) {
            seedForCase(calibrationCase.id());

            Map<String, String> titleByTicketId =
                    ticketRepository.findAll().stream()
                            .collect(
                                    Collectors.toMap(
                                            t -> t.getTicketId(), t -> t.getTitle()));

            List<ScoredChunk> keepSet = ragRetrievalService.retrieve(calibrationCase.query());
            Set<String> retrievedTitles = new HashSet<>();
            for (ScoredChunk chunk : keepSet) {
                String title = titleByTicketId.get(chunk.ticketId());
                if (title != null) {
                    retrievedTitles.add(title);
                }
            }

            RetrievalCalibrationScore.Result score =
                    RetrievalCalibrationScore.score(
                            calibrationCase.id(),
                            calibrationCase.mustIncludeTitles(),
                            calibrationCase.mustExcludeTitles(),
                            retrievedTitles);

            assertThat(score.passed())
                    .as("Calibration case %s", calibrationCase.id())
                    .isTrue();
        }
    }

    private void seedForCase(String caseId) {
        transactionTemplate.executeWithoutResult(
                status -> {
                    if ("password-reset-calibration".equals(caseId)) {
                        var target =
                                ticketService.create(
                                        PasswordResetTicketCalibrationFixture.TARGET_TICKET);
                        ticketIngestionService.ingest(target.ticketId());
                        var decoy =
                                ticketService.create(
                                        RetrievalCalibrationDecoyFixture.DECOY_TICKET);
                        ticketIngestionService.ingest(decoy.ticketId());
                    } else if ("sso-login-calibration".equals(caseId)) {
                        var target =
                                ticketService.create(RetrievalCalibrationDecoyFixture.SSO_TICKET);
                        ticketIngestionService.ingest(target.ticketId());
                        var decoy =
                                ticketService.create(
                                        RetrievalCalibrationDecoyFixture.BILLING_DECOY);
                        ticketIngestionService.ingest(decoy.ticketId());
                    }
                });
    }
}