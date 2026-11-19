package com.example.rag.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ticket.domain.Comment;
import com.example.ticket.domain.Ticket;
import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class KnowledgeDocumentBuilderTest {

    private final KnowledgeDocumentBuilder builder = new KnowledgeDocumentBuilder();

    @Test
    void omitsBlankSectionsAndOrdersComments() {
        Ticket ticket = ticket("TKT-1001", "Login bug", "Desc", null);
        Comment older =
                comment(
                        "TKT-1001",
                        "alice",
                        "First",
                        Instant.parse("2026-01-01T00:00:00Z"));
        Comment newer =
                comment(
                        "TKT-1001",
                        null,
                        "Second",
                        Instant.parse("2026-01-02T00:00:00Z"));

        var document = builder.build(ticket, List.of(newer, older));

        assertThat(document.sections())
                .containsExactly("Login bug", "Desc", "alice: First", "Second");
    }

    private static Ticket ticket(String id, String title, String description, String resolution) {
        Ticket ticket = new Ticket();
        ticket.setTicketId(id);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setResolutionNotes(resolution);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(TicketPriority.MEDIUM);
        return ticket;
    }

    private static Comment comment(String ticketId, String author, String content, Instant at) {
        Comment comment = new Comment();
        comment.setId(UUID.randomUUID());
        comment.setTicketId(ticketId);
        comment.setAuthor(author);
        comment.setContent(content);
        comment.setCreatedAt(at);
        return comment;
    }
}