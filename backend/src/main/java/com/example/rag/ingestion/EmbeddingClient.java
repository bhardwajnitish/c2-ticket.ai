package com.example.rag.ingestion;

import java.util.List;

public interface EmbeddingClient {

    List<float[]> embed(List<String> texts);

    String modelName();
}