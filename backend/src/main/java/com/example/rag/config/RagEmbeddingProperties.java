package com.example.rag.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rag.embedding")
public record RagEmbeddingProperties(
        @NotBlank String provider,
        @NotBlank String model,
        @Min(1) int dimension,
        @NotBlank String baseUrl) {}