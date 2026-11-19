package com.example.eval;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class GoldenDatasetEvaluationTest {

    @Test
    void goldenDatasetContainsFourNormativeCases() throws Exception {
        var manifest = EvalResourceSupport.loadGoldenDataset();

        assertThat(manifest.version()).isEqualTo("1.0");
        assertThat(manifest.cases()).hasSize(4);
        Set<String> ids =
                manifest.cases().stream().map(GoldenDatasetCase::id).collect(Collectors.toSet());
        assertThat(ids)
                .containsExactlyInAnyOrder(
                        "retrieval-expected-tickets",
                        "retrieval-below-threshold",
                        "grounding-supported-answer",
                        "hallucination-duration-expiry");

        for (GoldenDatasetCase goldenCase : manifest.cases()) {
            assertThat(goldenCase.id()).isNotBlank();
            assertThat(goldenCase.kind()).isNotBlank();
            assertThat(goldenCase.description()).isNotBlank();
        }
    }
}