package com.example.rag.retrieval;

public record ScoredChunk(String ticketId, int chunkIndex, String content, double similarity) {}