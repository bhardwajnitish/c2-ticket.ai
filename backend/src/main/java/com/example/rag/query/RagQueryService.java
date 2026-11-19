package com.example.rag.query;

import com.example.rag.api.RagQueryOutcome;
import com.example.rag.api.RagResponseMapper;
import com.example.rag.api.dto.RagQueryResponse;
import com.example.rag.context.RagContextBuilder;
import com.example.rag.generation.GeneratorClient;
import com.example.rag.generation.GeneratorException;
import com.example.rag.generation.GeneratorPromptBuilder;
import com.example.rag.grounding.ContextChunk;
import com.example.rag.grounding.RagGroundingGate;
import com.example.rag.ingestion.EmbeddingException;
import com.example.rag.retrieval.RagRetrievalService;
import com.example.rag.retrieval.ScoredChunk;
import com.example.rag.retrieval.VectorStoreException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RagQueryService {

    private static final Logger log = LoggerFactory.getLogger(RagQueryService.class);

    private final RagRetrievalService ragRetrievalService;
    private final RagContextBuilder ragContextBuilder;
    private final GeneratorPromptBuilder generatorPromptBuilder;
    private final GeneratorClient generatorClient;
    private final RagGroundingGate ragGroundingGate;
    private final CitationAssembler citationAssembler;
    private final RagResponseMapper responseMapper;

    public RagQueryService(
            RagRetrievalService ragRetrievalService,
            RagContextBuilder ragContextBuilder,
            GeneratorPromptBuilder generatorPromptBuilder,
            GeneratorClient generatorClient,
            RagGroundingGate ragGroundingGate,
            CitationAssembler citationAssembler,
            RagResponseMapper responseMapper) {
        this.ragRetrievalService = ragRetrievalService;
        this.ragContextBuilder = ragContextBuilder;
        this.generatorPromptBuilder = generatorPromptBuilder;
        this.generatorClient = generatorClient;
        this.ragGroundingGate = ragGroundingGate;
        this.citationAssembler = citationAssembler;
        this.responseMapper = responseMapper;
    }

    public RagQueryResponse query(String trimmedQuery) {
        try {
            List<ScoredChunk> keepSet = ragRetrievalService.retrieve(trimmedQuery);
            if (keepSet.isEmpty()) {
                return responseMapper.noRelevantTickets(trimmedQuery);
            }

            String context = ragContextBuilder.buildFromKeepSet(keepSet);
            String prompt = generatorPromptBuilder.build(trimmedQuery, context);

            String draftAnswer;
            try {
                draftAnswer = generatorClient.generate(prompt);
            } catch (GeneratorException ex) {
                log.warn("RAG generation failed", ex);
                return responseMapper.error(
                        trimmedQuery,
                        "GENERATION_FAILED",
                        "Unable to generate an answer at this time.");
            }

            Set<String> keepSetTicketIds = ticketIdsFrom(keepSet);
            List<ContextChunk> contextChunks = toContextChunks(keepSet);
            if (!ragGroundingGate.passes(draftAnswer, keepSetTicketIds, contextChunks)) {
                return responseMapper.groundingGateFailed(trimmedQuery);
            }

            double bestSimilarity =
                    keepSet.stream().mapToDouble(ScoredChunk::similarity).max().orElse(0.0);
            var citations = citationAssembler.assemble(keepSet);
            return responseMapper.answer(trimmedQuery, draftAnswer, citations, bestSimilarity);
        } catch (EmbeddingException ex) {
            log.warn("RAG query embedding failed", ex);
            return responseMapper.error(
                    trimmedQuery, "EMBEDDING_UNAVAILABLE", "Query embedding is unavailable.");
        } catch (VectorStoreException ex) {
            log.warn("RAG vector search failed", ex);
            return responseMapper.error(
                    trimmedQuery, "VECTOR_STORE_UNAVAILABLE", "Ticket search is unavailable.");
        } catch (Exception ex) {
            log.error("Unexpected RAG query failure", ex);
            return responseMapper.error(
                    trimmedQuery, "INTERNAL_ERROR", "Unable to process your question.");
        }
    }

    public static RagQueryOutcome outcomeOf(RagQueryResponse response) {
        return response.outcome();
    }

    private static Set<String> ticketIdsFrom(List<ScoredChunk> keepSet) {
        Set<String> ids = new HashSet<>();
        for (ScoredChunk chunk : keepSet) {
            ids.add(chunk.ticketId());
        }
        return ids;
    }

    private static List<ContextChunk> toContextChunks(List<ScoredChunk> keepSet) {
        return keepSet.stream()
                .map(chunk -> new ContextChunk(chunk.ticketId(), chunk.content()))
                .toList();
    }
}