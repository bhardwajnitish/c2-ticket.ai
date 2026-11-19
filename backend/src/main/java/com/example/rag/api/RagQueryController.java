package com.example.rag.api;

import com.example.rag.api.dto.RagQueryRequest;
import com.example.rag.api.dto.RagQueryResponse;
import com.example.rag.query.RagQueryService;
import com.example.ticket.api.ValidationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag")
public class RagQueryController {

    private final RagQueryService ragQueryService;

    public RagQueryController(RagQueryService ragQueryService) {
        this.ragQueryService = ragQueryService;
    }

    @PostMapping("/query")
    public ResponseEntity<RagQueryResponse> query(@Valid @RequestBody RagQueryRequest request) {
        String trimmed = request.query() == null ? "" : request.query().trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException("query must not be blank.");
        }
        RagQueryResponse response = ragQueryService.query(trimmed);
        HttpStatus status =
                response.outcome() == RagQueryOutcome.ERROR
                        ? HttpStatus.SERVICE_UNAVAILABLE
                        : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}