package com.example.rag.ingestion;

import com.example.rag.config.RagEmbeddingProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestEmbeddingClient implements EmbeddingClient {

    private final RagEmbeddingProperties embeddingProperties;

    public TestEmbeddingClient(RagEmbeddingProperties embeddingProperties) {
        this.embeddingProperties = embeddingProperties;
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        List<float[]> vectors = new ArrayList<>();
        for (String text : texts) {
            vectors.add(deterministicVector(text, embeddingProperties.dimension()));
        }
        return vectors;
    }

    @Override
    public String modelName() {
        return embeddingProperties.model();
    }

    static float[] deterministicVector(String text, int dimension) {
        float[] vector = new float[dimension];
        if (text == null || text.isBlank()) {
            return vector;
        }
        String normalized = text.toLowerCase(Locale.ROOT).trim();
        for (String token : normalized.split("\\W+")) {
            if (token.length() < 2) {
                continue;
            }
            float weight = Math.min(2.0f, token.length() / 4.0f);
            sprinkleToken(vector, token, dimension, weight);
        }
        normalize(vector);
        return vector;
    }

    private static void sprinkleToken(float[] vector, String token, int dimension) {
        sprinkleToken(vector, token, dimension, 1.0f);
    }

    private static void sprinkleToken(
            float[] vector, String token, int dimension, float weight) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            for (int i = 0; i < hash.length; i++) {
                int index = (hash[i] & 0xff) % dimension;
                vector[index] += weight;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void normalize(float[] vector) {
        double sumSq = 0;
        for (float v : vector) {
            sumSq += v * v;
        }
        double norm = Math.sqrt(sumSq);
        if (norm == 0) {
            return;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / norm);
        }
    }
}