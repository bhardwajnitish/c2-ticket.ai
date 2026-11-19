package com.example.rag.retrieval;

import java.util.List;

public interface VectorSearchPort {

    List<VectorNeighbor> searchByCosineDistance(float[] queryEmbedding, int limit);
}