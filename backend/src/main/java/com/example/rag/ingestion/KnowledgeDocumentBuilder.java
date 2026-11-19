package com.example.rag.ingestion;

import com.example.ticket.domain.Comment;
import com.example.ticket.domain.Ticket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeDocumentBuilder {

    public KnowledgeDocument build(Ticket ticket, List<Comment> comments) {
        List<String> sections = new ArrayList<>();

        if (isNonBlank(ticket.getTitle())) {
            sections.add(ticket.getTitle().trim());
        }
        if (isNonBlank(ticket.getDescription())) {
            sections.add(ticket.getDescription().trim());
        }

        List<Comment> ordered =
                comments.stream()
                        .sorted(
                                Comparator.comparing(
                                        Comment::getCreatedAt,
                                        Comparator.nullsLast(Comparator.naturalOrder())))
                        .toList();
        for (Comment comment : ordered) {
            String line = formatCommentLine(comment);
            if (isNonBlank(line)) {
                sections.add(line);
            }
        }

        if (isNonBlank(ticket.getResolutionNotes())) {
            sections.add(ticket.getResolutionNotes().trim());
        }

        String fullText = String.join("\n\n", sections);
        return new KnowledgeDocument(sections, fullText);
    }

    private static String formatCommentLine(Comment comment) {
        String content = comment.getContent() != null ? comment.getContent().replaceAll("\\s+", " ").trim() : "";
        if (content.isEmpty()) {
            return "";
        }
        if (isNonBlank(comment.getAuthor())) {
            return comment.getAuthor().trim() + ": " + content;
        }
        return content;
    }

    private static boolean isNonBlank(String value) {
        return value != null && !value.isBlank();
    }

    public record KnowledgeDocument(List<String> sections, String fullText) {}
}