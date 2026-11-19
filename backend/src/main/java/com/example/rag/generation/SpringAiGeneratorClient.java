package com.example.rag.generation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class SpringAiGeneratorClient implements GeneratorClient {

    private final ChatClient chatClient;

    public SpringAiGeneratorClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String generate(String prompt) {
        try {
            String text =
                    chatClient
                            .prompt()
                            .user(prompt)
                            .call()
                            .content();
            if (text == null || text.isBlank()) {
                throw new GeneratorException("LLM returned empty response");
            }
            return text.trim();
        } catch (GeneratorException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new GeneratorException("Generation request failed", ex);
        }
    }
}