package com.example.rag.api;

import com.example.rag.api.dto.RagAnswerDto;
import com.example.rag.api.dto.RagCitationDto;
import com.example.rag.api.dto.RagConfidenceDto;
import com.example.rag.api.dto.RagEscalationDto;
import com.example.rag.api.dto.RagErrorDto;
import com.example.rag.api.dto.RagNoMatchDto;
import com.example.rag.api.dto.RagQueryResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RagResponseMapper {

    public static final String NO_RELEVANT_TICKETS = "NO_RELEVANT_TICKETS";
    public static final String GROUNDING_GATE_FAILED = "GROUNDING_GATE_FAILED";

    public RagQueryResponse noRelevantTickets(String query) {
        return new RagQueryResponse(
                RagQueryOutcome.NO_MATCH,
                query,
                null,
                List.of(),
                null,
                new RagEscalationDto(true, null),
                null,
                new RagNoMatchDto(NO_RELEVANT_TICKETS, "No tickets matched your question."));
    }

    public RagQueryResponse groundingGateFailed(String query) {
        return new RagQueryResponse(
                RagQueryOutcome.NO_MATCH,
                query,
                null,
                List.of(),
                null,
                new RagEscalationDto(true, null),
                null,
                new RagNoMatchDto(
                        GROUNDING_GATE_FAILED,
                        "Answer could not be verified against retrieved tickets."));
    }

    public RagQueryResponse answer(
            String query, String answerText, List<RagCitationDto> citations, double bestSimilarity) {
        return new RagQueryResponse(
                RagQueryOutcome.ANSWER,
                query,
                new RagAnswerDto(answerText),
                List.copyOf(citations),
                new RagConfidenceDto(bestSimilarity, "RETRIEVAL"),
                new RagEscalationDto(false, null),
                null,
                null);
    }

    public RagQueryResponse error(String query, String code, String message) {
        return new RagQueryResponse(
                RagQueryOutcome.ERROR,
                query,
                null,
                List.of(),
                null,
                null,
                new RagErrorDto(code, message),
                null);
    }
}