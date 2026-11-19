package com.example.rag.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record RagEscalationDto(
        @JsonProperty("recommended") boolean recommended,
        @JsonProperty("reason") String reason) {}