package com.example.config;

import com.example.rag.config.RagChunkProperties;
import com.example.rag.config.RagEmbeddingProperties;
import com.example.rag.config.RagRetrievalProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@Validated
@EnableConfigurationProperties({
    AppCorsProperties.class,
    RagRetrievalProperties.class,
    RagChunkProperties.class,
    RagEmbeddingProperties.class
})
public class PropertiesValidationConfiguration {}