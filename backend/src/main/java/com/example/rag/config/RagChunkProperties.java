package com.example.rag.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rag.chunk")
public record RagChunkProperties(
        @Min(1) int maxCharacters,
        @Min(0) int overlapCharacters,
        @Min(1) int maxTokens,
        @Min(0) int overlapTokens,
        @NotBlank String version) {}