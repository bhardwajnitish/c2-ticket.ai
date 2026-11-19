package com.example.ticket.api;

import java.util.List;

public record ErrorBody(String code, String message, List<ErrorDetail> details) {}