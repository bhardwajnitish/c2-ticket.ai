package com.example.rag.ingestion;

import com.example.ticket.domain.Ticket;
import com.example.ticket.domain.TicketPriority;
import com.example.ticket.domain.TicketStatus;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class ContentFingerprint {

    private ContentFingerprint() {}

    public static String compute(
            String content,
            Ticket ticket,
            String chunkerVersion,
            String embeddingModel) {
        String canonical =
                String.join(
                        "|",
                        content,
                        ticket.getTicketId(),
                        ticket.getStatus().name(),
                        ticket.getPriority().name(),
                        nullToEmpty(ticket.getAssignee()),
                        nullToEmpty(ticket.getCategory()),
                        chunkerVersion,
                        embeddingModel);
        return sha256Hex(canonical);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}