package com.example.rag.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record RagQueryRequest(@JsonProperty("query") @NotNull String query) {}