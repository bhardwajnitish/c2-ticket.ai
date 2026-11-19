package com.example.rag.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.rag.retrieval.ScoredChunk;
import com.example.ticket.domain.Ticket;
import com.example.ticket.repository.TicketRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CitationAssemblerTest {

    @Mock private TicketRepository ticketRepository;

    @InjectMocks private CitationAssembler citationAssembler;

    @Test
    void ordersByBestSimilarityThenTicketId() {
        List<ScoredChunk> keepSet =
                List.of(
                        new ScoredChunk("TKT-1002", 0, "b", 0.8),
                        new ScoredChunk("TKT-1001", 0, "a", 0.9),
                        new ScoredChunk("TKT-1002", 1, "b2", 0.85));

        Ticket t1 = new Ticket();
        t1.setTicketId("TKT-1001");
        t1.setTitle("Alpha");
        Ticket t2 = new Ticket();
        t2.setTicketId("TKT-1002");
        t2.setTitle("Beta");

        when(ticketRepository.findByTicketIdIn(any())).thenReturn(List.of(t1, t2));

        var citations = citationAssembler.assemble(keepSet);

        assertThat(citations).hasSize(2);
        assertThat(citations.get(0).ticketId()).isEqualTo("TKT-1001");
        assertThat(citations.get(0).title()).isEqualTo("Alpha");
        assertThat(citations.get(1).ticketId()).isEqualTo("TKT-1002");
    }
}