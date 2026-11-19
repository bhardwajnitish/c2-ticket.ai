CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ticket_chunk (
    id BIGSERIAL PRIMARY KEY,
    ticket_id VARCHAR(32) NOT NULL,
    chunk_index INTEGER NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    assignee VARCHAR(255),
    category VARCHAR(255),
    content_fingerprint VARCHAR(64) NOT NULL,
    embedding_model VARCHAR(128) NOT NULL,
    chunker_version VARCHAR(32) NOT NULL,
    ingested_at TIMESTAMPTZ NOT NULL,
    embedding vector(768) NOT NULL
);

CREATE INDEX ix_ticket_chunk_ticket_id ON ticket_chunk (ticket_id);

CREATE INDEX ix_ticket_chunk_embedding ON ticket_chunk
    USING hnsw (embedding vector_cosine_ops);