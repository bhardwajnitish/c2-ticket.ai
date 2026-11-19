package com.example.eval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TicketApiContractEvaluationTest {

    private static final Set<String> TICKET_KEYS =
            Set.of(
                    "ticketId",
                    "title",
                    "description",
                    "priority",
                    "status",
                    "assignee",
                    "category",
                    "resolutionNotes",
                    "createdAt",
                    "updatedAt");

    private static final Set<String> ERROR_KEYS = Set.of("code", "message", "details");

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void createTicketResponseMatchesContract() throws Exception {
        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/tickets")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"title\":\"Contract ticket\",\"description\":\"d\",\"priority\":\"LOW\"}"))
                        .andExpect(status().isCreated())
                        .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.fieldNames()).toIterable().containsExactlyInAnyOrderElementsOf(TICKET_KEYS);
        assertThat(body.has("id")).isFalse();
        assertThat(body.get("ticketId").asText()).matches("^TKT-[0-9]+$");
    }

    @Test
    void notFoundUsesTicketErrorEnvelope() throws Exception {
        MvcResult result =
                mockMvc.perform(get("/api/v1/tickets/TKT-999999"))
                        .andExpect(status().isNotFound())
                        .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.has("error")).isTrue();
        JsonNode error = body.get("error");
        assertThat(error.fieldNames()).toIterable().containsAll(ERROR_KEYS);
        assertThat(error.get("code").asText()).isEqualTo("TICKET_NOT_FOUND");
        assertThat(error.get("details").isArray()).isTrue();
    }
}