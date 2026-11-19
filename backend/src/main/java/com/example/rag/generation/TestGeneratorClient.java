package com.example.rag.generation;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestGeneratorClient implements GeneratorClient {

    @Override
    public String generate(String prompt) {
        if (prompt.contains("expire after 30 minutes")
                && prompt.contains("How long do password reset links last")) {
            throw new GeneratorException("Test mode: use @MockBean GeneratorClient for hallucination scenarios");
        }
        return "According to the retrieved tickets, please see the cited ticket context for details.";
    }
}