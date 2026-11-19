package com.example.rag.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.rag.api.RagQueryOutcome;
import com.example.rag.api.RagResponseMapper;
import com.example.rag.context.RagContextBuilder;
import com.example.rag.generation.GeneratorClient;
import com.example.rag.generation.GeneratorPromptBuilder;
import com.example.rag.grounding.CitationValidator;
import com.example.rag.grounding.RagAnswerGroundingReviewer;
import com.example.rag.grounding.RagGroundingGate;
import com.example.rag.retrieval.RagRetrievalService;
import com.example.ticket.repository.TicketRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RagQueryServiceTest {

    @Mock private RagRetrievalService ragRetrievalService;
    @Mock private GeneratorClient generatorClient;
    @Mock private TicketRepository ticketRepository;

    @Test
    void emptyKeepSetSkipsGenerator() {
        when(ragRetrievalService.retrieve("mars refund")).thenReturn(List.of());

        RagQueryService service = buildService();
        var response = service.query("mars refund");

        assertThat(response.outcome()).isEqualTo(RagQueryOutcome.NO_MATCH);
        assertThat(response.noMatch().code()).isEqualTo(RagResponseMapper.NO_RELEVANT_TICKETS);
        verify(generatorClient, never()).generate(anyString());
    }

    private RagQueryService buildService() {
        return new RagQueryService(
                ragRetrievalService,
                new RagContextBuilder(),
                new GeneratorPromptBuilder(),
                generatorClient,
                new RagGroundingGate(new CitationValidator(), new RagAnswerGroundingReviewer()),
                new CitationAssembler(ticketRepository),
                new RagResponseMapper());
    }
}