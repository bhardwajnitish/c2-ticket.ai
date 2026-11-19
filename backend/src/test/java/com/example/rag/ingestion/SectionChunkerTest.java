package com.example.rag.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.rag.config.RagChunkProperties;
import com.example.ticket.domain.Ticket;
import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;
import org.junit.jupiter.api.Test;

class SectionChunkerTest {

    @Test
    void emitsSingleChunkWhenDocumentFits() {
        SectionChunker chunker = new SectionChunker(new RagChunkProperties(2000, 250, 512, 64, "section-v1"));
        Ticket ticket = ticket("TKT-1001", "Title");
        var doc = new KnowledgeDocumentBuilder.KnowledgeDocument(java.util.List.of("A", "B"), "A\n\nB");

        var bodies = chunker.chunkBodies(ticket, doc);

        assertThat(bodies).hasSize(1);
        String content = chunker.toChunkContent(ticket, bodies.get(0));
        assertThat(content).startsWith("[Ticket TKT-1001] Title\n");
    }

    @Test
    void splitsOversizedSectionWithOverlap() {
        SectionChunker chunker = new SectionChunker(new RagChunkProperties(50, 10, 512, 64, "section-v1"));
        Ticket ticket = ticket("TKT-1002", "Long");
        String big = "x".repeat(120);
        var doc = new KnowledgeDocumentBuilder.KnowledgeDocument(java.util.List.of(big), big);

        var bodies = chunker.chunkBodies(ticket, doc);

        assertThat(bodies.size()).isGreaterThan(1);
        assertThat(chunker.prefix(ticket)).isEqualTo("[Ticket TKT-1002] Long\n");
    }

    private static Ticket ticket(String id, String title) {
        Ticket ticket = new Ticket();
        ticket.setTicketId(id);
        ticket.setTitle(title);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        return ticket;
    }
}