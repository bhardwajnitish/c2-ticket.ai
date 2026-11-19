package com.example.rag.retrieval;

public record VectorNeighbor(
        String ticketId, int chunkIndex, String content, double cosineDistance) {}