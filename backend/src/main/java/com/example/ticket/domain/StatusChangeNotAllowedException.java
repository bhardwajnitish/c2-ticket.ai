package com.example.ticket.domain;

public class StatusChangeNotAllowedException extends RuntimeException {

    public StatusChangeNotAllowedException() {
        super("Status cannot be changed on this endpoint.");
    }
}