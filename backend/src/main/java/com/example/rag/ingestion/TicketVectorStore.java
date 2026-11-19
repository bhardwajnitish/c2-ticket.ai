package com.example.rag.ingestion;

import java.util.List;

public interface TicketVectorStore {

    List<ChunkFingerprintRow> findFingerprintsByTicketId(String ticketId);

    void replaceChunks(String ticketId, List<TicketChunkRecord> chunks);

    void deleteByTicketId(String ticketId);
}