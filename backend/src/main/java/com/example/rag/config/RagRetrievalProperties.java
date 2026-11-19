package com.example.rag.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "rag.retrieval")
public record RagRetrievalProperties(
        @Min(1) int topK,
        @DecimalMin("0.0") @DecimalMax("1.0") double similarityThreshold) {}