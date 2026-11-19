package com.example.rag.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.rag.generation.GeneratorClient;
import com.example.rag.retrieval.RagRetrievalService;
import com.example.rag.support.PasswordResetKnowledgeFixture;
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
class RagQueryGroundingRegressionTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private RagRetrievalService ragRetrievalService;

    @MockBean private GeneratorClient generatorClient;

    @Test
    void apiDoesNotReturn24HourSentenceAsAnswer() throws Exception {
        when(ragRetrievalService.retrieve(PasswordResetKnowledgeFixture.QUERY))
                .thenReturn(PasswordResetKnowledgeFixture.KEEP_SET);
        when(generatorClient.generate(anyString()))
                .thenReturn(PasswordResetKnowledgeFixture.HALLUCINATED_ANSWER);

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/rag/query")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"query\":\""
                                                        + PasswordResetKnowledgeFixture.QUERY
                                                        + "\"}"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.outcome").value("NO_MATCH"))
                        .andExpect(jsonPath("$.answer").isEmpty())
                        .andExpect(jsonPath("$.noMatch.code").value("GROUNDING_GATE_FAILED"))
                        .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("24 hours");
        assertThat(body).doesNotContain(PasswordResetKnowledgeFixture.HALLUCINATED_ANSWER);
    }
}