package com.example.ticket.api;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}