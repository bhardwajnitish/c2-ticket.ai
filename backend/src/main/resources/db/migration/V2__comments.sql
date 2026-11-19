CREATE TABLE ticket_comment (
    id UUID PRIMARY KEY,
    ticket_id VARCHAR(32) NOT NULL,
    author VARCHAR(255),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_comment_ticket FOREIGN KEY (ticket_id) REFERENCES ticket (ticket_id)
);