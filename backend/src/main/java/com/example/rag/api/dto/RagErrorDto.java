package com.example.rag.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record RagErrorDto(
        @JsonProperty("code") String code, @JsonProperty("message") String message) {}