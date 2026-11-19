package com.example.rag.retrieval;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RagRetrievalServicePolicyLiteralsTest {

    @Test
    void mustNotHardcodeTopKOrSimilarityThresholdLiterals() throws Exception {
        Path source =
                Path.of("src/main/java/com/example/rag/retrieval/RagRetrievalService.java");
        String content = Files.readString(source);

        assertFalse(
                content.contains("0.72"),
                "RagRetrievalService must not contain literal 0.72; use RagRetrievalProperties");

        assertFalse(
                content.matches("(?s).*\\blimit\\(\\s*5\\s*\\).*"),
                "RagRetrievalService must not hardcode limit(5); use retrievalProperties.topK()");
        assertFalse(
                content.matches("(?s).*\\btopK\\s*=\\s*5\\s*;.*"),
                "RagRetrievalService must not assign topK = 5");
    }
}