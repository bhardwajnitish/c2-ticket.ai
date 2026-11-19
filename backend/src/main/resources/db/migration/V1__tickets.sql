CREATE TABLE ticket (
    id BIGSERIAL PRIMARY KEY,
    ticket_id VARCHAR(32) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(10000),
    resolution_notes VARCHAR(10000),
    status VARCHAR(32) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    assignee VARCHAR(255),
    category VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_ticket_ticket_id UNIQUE (ticket_id),
    CONSTRAINT ck_ticket_status CHECK (
        status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'CANCELLED')
    ),
    CONSTRAINT ck_ticket_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT ck_ticket_title_len CHECK (char_length(title) BETWEEN 1 AND 255),
    CONSTRAINT ck_ticket_description_len CHECK (
        description IS NULL OR char_length(description) <= 10000
    ),
    CONSTRAINT ck_ticket_resolution_notes_len CHECK (
        resolution_notes IS NULL OR char_length(resolution_notes) <= 10000
    ),
    CONSTRAINT ck_ticket_ticket_id_format CHECK (ticket_id ~ '^TKT-[0-9]+$')
);