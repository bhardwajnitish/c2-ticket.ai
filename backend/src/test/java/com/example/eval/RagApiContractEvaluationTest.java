package com.example.eval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.rag.generation.GeneratorClient;
import com.example.rag.retrieval.RagRetrievalService;
import com.example.ticket.domain.Ticket;
import com.example.ticket.repository.TicketRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RagApiContractEvaluationTest {

    private static final Set<String> RAG_ENVELOPE_KEYS =
            Set.of(
                    "outcome",
                    "query",
                    "answer",
                    "citations",
                    "confidence",
                    "escalation",
                    "error",
                    "noMatch");

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockBean private RagRetrievalService ragRetrievalService;

    @MockBean private GeneratorClient generatorClient;

    @MockBean private TicketRepository ticketRepository;

    @Test
    void noMatchEmptyKeepSetHasFullEnvelopeAndSkipsGenerator() throws Exception {
        when(ragRetrievalService.retrieve(anyString())).thenReturn(List.of());

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/rag/query")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"query\":\"unknown topic\"}"))
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.fieldNames()).toIterable().containsAll(RAG_ENVELOPE_KEYS);
        assertThat(body.get("outcome").asText()).isEqualTo("NO_MATCH");
        assertThat(body.get("noMatch").get("code").asText()).isEqualTo("NO_RELEVANT_TICKETS");
        assertThat(body.get("citations").isArray()).isTrue();
        assertThat(body.get("citations")).isEmpty();
        assertThat(body.get("answer").isNull()).isTrue();
        verify(generatorClient, never()).generate(anyString());
    }

    @Test
    void answerOutcomeIncludesConfidenceAndCitationsArray() throws Exception {
        when(ragRetrievalService.retrieve(anyString()))
                .thenReturn(
                        List.of(
                                new com.example.rag.retrieval.ScoredChunk(
                                        "TKT-1001", 0, "context", 0.88)));
        when(generatorClient.generate(anyString()))
                .thenReturn("According to TKT-1001, links expire after 30 minutes.");
        Ticket ticket = new Ticket();
        ticket.setTicketId("TKT-1001");
        ticket.setTitle("Password reset");
        when(ticketRepository.findByTicketIdIn(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(List.of(ticket));

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/rag/query")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"query\":\"reset links\"}"))
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.fieldNames()).toIterable().containsAll(RAG_ENVELOPE_KEYS);
        assertThat(body.get("outcome").asText()).isEqualTo("ANSWER");
        assertThat(body.get("confidence").get("basis").asText()).isEqualTo("RETRIEVAL");
        assertThat(body.get("citations").isArray()).isTrue();
    }
}