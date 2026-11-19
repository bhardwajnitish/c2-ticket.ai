package com.example.ticket.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @Size(max = 255) String author, @NotBlank @Size(max = 10000) String content) {}