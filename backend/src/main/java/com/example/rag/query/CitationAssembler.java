package com.example.rag.query;

import com.example.rag.api.dto.RagCitationDto;
import com.example.rag.retrieval.ScoredChunk;
import com.example.ticket.domain.Ticket;
import com.example.ticket.repository.TicketRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CitationAssembler {

    private final TicketRepository ticketRepository;

    public CitationAssembler(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<RagCitationDto> assemble(List<ScoredChunk> keepSet) {
        Map<String, Double> bestSimilarityByTicket = new HashMap<>();
        for (ScoredChunk chunk : keepSet) {
            bestSimilarityByTicket.merge(
                    chunk.ticketId(), chunk.similarity(), Math::max);
        }

        List<String> orderedTicketIds =
                new ArrayList<>(bestSimilarityByTicket.keySet());
        orderedTicketIds.sort(
                Comparator.<String, Double>comparing(ticketId -> bestSimilarityByTicket.get(ticketId))
                        .reversed()
                        .thenComparing(ticketId -> ticketId));

        List<Ticket> tickets = ticketRepository.findByTicketIdIn(orderedTicketIds);
        Map<String, String> titleByTicketId = new HashMap<>();
        for (Ticket ticket : tickets) {
            titleByTicketId.put(ticket.getTicketId(), ticket.getTitle());
        }

        List<RagCitationDto> citations = new ArrayList<>();
        for (String ticketId : orderedTicketIds) {
            String title = titleByTicketId.getOrDefault(ticketId, ticketId);
            citations.add(new RagCitationDto(ticketId, title));
        }
        return List.copyOf(citations);
    }
}