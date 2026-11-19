package com.example.rag.ingestion;

import com.example.rag.config.RagEmbeddingProperties;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class SpringAiEmbeddingClient implements EmbeddingClient {

    private final EmbeddingModel embeddingModel;
    private final RagEmbeddingProperties embeddingProperties;

    public SpringAiEmbeddingClient(
            EmbeddingModel embeddingModel, RagEmbeddingProperties embeddingProperties) {
        this.embeddingModel = embeddingModel;
        this.embeddingProperties = embeddingProperties;
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        try {
            List<float[]> vectors = new ArrayList<>();
            for (float[] vector : embeddingModel.embed(texts)) {
                vectors.add(vector);
            }
            return vectors;
        } catch (Exception ex) {
            throw new EmbeddingException("Embedding request failed", ex);
        }
    }

    @Override
    public String modelName() {
        return embeddingProperties.model();
    }
}