package com.example.rag.ingestion;

import com.example.rag.config.RagChunkProperties;
import com.example.ticket.domain.Ticket;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SectionChunker {

    private final RagChunkProperties chunkProperties;

    public SectionChunker(RagChunkProperties chunkProperties) {
        this.chunkProperties = chunkProperties;
    }

    public List<String> chunkBodies(Ticket ticket, KnowledgeDocumentBuilder.KnowledgeDocument document) {
        int maxChars = chunkProperties.maxCharacters();
        List<String> sections = document.sections();
        if (sections.isEmpty()) {
            return List.of();
        }

        if (document.fullText().length() <= maxChars) {
            return List.of(document.fullText());
        }

        List<String> bodies = new ArrayList<>();
        for (String section : sections) {
            if (section.length() <= maxChars) {
                bodies.add(section);
            } else {
                bodies.addAll(splitWithOverlap(section, maxChars, chunkProperties.overlapCharacters()));
            }
        }
        return bodies;
    }

    public String prefix(Ticket ticket) {
        return "[Ticket " + ticket.getTicketId() + "] " + ticket.getTitle() + "\n";
    }

    public String toChunkContent(Ticket ticket, String body) {
        return prefix(ticket) + body;
    }

    private static List<String> splitWithOverlap(String text, int maxChars, int overlap) {
        List<String> parts = new ArrayList<>();
        int step = Math.max(1, maxChars - overlap);
        int offset = 0;
        while (offset < text.length()) {
            int end = Math.min(offset + maxChars, text.length());
            parts.add(text.substring(offset, end));
            if (end >= text.length()) {
                break;
            }
            offset += step;
        }
        return parts;
    }
}