package com.example.eval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GoldenDatasetCase(
        String id,
        String kind,
        String description,
        String query,
        List<SeedTicket> seedTickets,
        List<String> expectKeepSetTitles,
        List<String> expectExcludedTitles,
        List<ThresholdCandidate> candidates,
        List<String> expectKeepSetTicketIds,
        List<String> expectExcludedTicketIds,
        List<ContextChunkRef> contextChunks,
        String generatorOutput,
        String expectOutcome,
        String expectNoMatchCode,
        List<String> expectCitationTicketIds) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SeedTicket(String title, String description) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ThresholdCandidate(String ticketId, double similarity) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContextChunkRef(String ticketId, String content) {}
}