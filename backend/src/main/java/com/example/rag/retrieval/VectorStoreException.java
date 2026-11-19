package com.example.rag.retrieval;

public class VectorStoreException extends RuntimeException {

    public VectorStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}