package com.example.rag.generation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GeneratorPromptBuilderTest {

    private final GeneratorPromptBuilder builder = new GeneratorPromptBuilder();

    @Test
    void includesSection81InstructionsVerbatim() {
        String prompt = builder.build("What is TKT-1001?", "chunk text");

        assertThat(prompt).startsWith(GeneratorPromptBuilder.GROUNDING_INSTRUCTIONS_VERBATIM);
        assertThat(prompt).contains("User question: What is TKT-1001?");
        assertThat(prompt).contains("chunk text");
    }
}