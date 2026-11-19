package com.example.eval;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

final class EvalResourceSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private EvalResourceSupport() {}

    static GoldenDatasetManifest loadGoldenDataset() throws IOException {
        try (InputStream stream =
                resourceStream("eval/golden-dataset.json")) {
            return MAPPER.readValue(stream, GoldenDatasetManifest.class);
        }
    }

    static RetrievalCalibrationManifest loadRetrievalCalibrationQueries() throws IOException {
        try (InputStream stream = resourceStream("eval/retrieval-calibration-queries.json")) {
            return MAPPER.readValue(stream, RetrievalCalibrationManifest.class);
        }
    }

    static JsonNode loadJson(String classpathResource) throws IOException {
        try (InputStream stream = resourceStream(classpathResource)) {
            return MAPPER.readTree(stream);
        }
    }

    private static InputStream resourceStream(String path) {
        InputStream stream =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing classpath resource: " + path);
        }
        return stream;
    }

    record GoldenDatasetManifest(String version, List<GoldenDatasetCase> cases) {}

    record RetrievalCalibrationManifest(String version, List<RetrievalCalibrationCase> queries) {}
}