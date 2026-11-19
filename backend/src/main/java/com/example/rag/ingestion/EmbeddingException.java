package com.example.rag.ingestion;

public class EmbeddingException extends RuntimeException {

    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}