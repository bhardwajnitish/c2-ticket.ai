package com.example.ticket.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketApiMockMvcTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Test
    void createAndGetTicket() throws Exception {
        String body =
                """
                {"title":"Login bug","priority":"HIGH","assignee":"alice","category":"auth"}
                """;

        String createResponse =
                mockMvc.perform(
                                post("/api/v1/tickets")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.ticketId").value(org.hamcrest.Matchers.matchesPattern("^TKT-[0-9]+$")))
                        .andExpect(jsonPath("$.status").value("OPEN"))
                        .andExpect(jsonPath("$.priority").value("HIGH"))
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        JsonNode node = objectMapper.readTree(createResponse);
        String ticketId = node.get("ticketId").asText();

        mockMvc.perform(get("/api/v1/tickets/" + ticketId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Login bug"));
    }

    @Test
    void createRejectsUnknownPropertiesWithErrorEnvelope() throws Exception {
        mockMvc.perform(
                        post("/api/v1/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"title":"x","ticketId":"TKT-9999"}
                                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    void listFiltersWithAndSemantics() throws Exception {
        mockMvc.perform(
                        post("/api/v1/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"One\",\"assignee\":\"alice\",\"category\":\"billing\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/v1/tickets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"Two\",\"assignee\":\"bob\",\"category\":\"billing\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/tickets").param("assignee", "alice").param("category", "billing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("One"));
    }

    @Test
    void patchRejectsStatusOnGeneralEndpoint() throws Exception {
        String ticketId = createTicket("Patch me");

        mockMvc.perform(
                        patch("/api/v1/tickets/" + ticketId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error.code")
                                .value("STATUS_CHANGE_NOT_ALLOWED_ON_THIS_ENDPOINT"));
    }

    @Test
    void statusTransitionLegalAndIllegal() throws Exception {
        String ticketId = createTicket("Status flow");

        mockMvc.perform(
                        patch("/api/v1/tickets/" + ticketId + "/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(
                        patch("/api/v1/tickets/" + ticketId + "/status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_STATUS_TRANSITION"));

        String getBody =
                mockMvc.perform(get("/api/v1/tickets/" + ticketId))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertThat(objectMapper.readTree(getBody).get("status").asText()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void notFoundUsesErrorEnvelope() throws Exception {
        mockMvc.perform(get("/api/v1/tickets/TKT-99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TICKET_NOT_FOUND"))
                .andExpect(jsonPath("$.error.details").isArray());
    }

    @Test
    void commentsCrud() throws Exception {
        String ticketId = createTicket("With comments");

        mockMvc.perform(
                        post("/api/v1/tickets/" + ticketId + "/comments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"author\":\"alice\",\"content\":\"First note\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.ticketId").value(ticketId));

        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("First note"));
    }

    private String createTicket(String title) throws Exception {
        String response =
                mockMvc.perform(
                                post("/api/v1/tickets")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content("{\"title\":\"" + title + "\"}"))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();
        return objectMapper.readTree(response).get("ticketId").asText();
    }
}