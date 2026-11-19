package com.example.rag.api.dto;

import com.example.rag.api.RagQueryOutcome;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record RagQueryResponse(
        @JsonProperty("outcome") RagQueryOutcome outcome,
        @JsonProperty("query") String query,
        @JsonProperty("answer") RagAnswerDto answer,
        @JsonProperty("citations") List<RagCitationDto> citations,
        @JsonProperty("confidence") RagConfidenceDto confidence,
        @JsonProperty("escalation") RagEscalationDto escalation,
        @JsonProperty("error") RagErrorDto error,
        @JsonProperty("noMatch") RagNoMatchDto noMatch) {}