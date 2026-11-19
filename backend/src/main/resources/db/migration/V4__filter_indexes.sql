CREATE INDEX ix_ticket_status ON ticket (status);
CREATE INDEX ix_ticket_priority ON ticket (priority);
CREATE INDEX ix_ticket_assignee ON ticket (assignee);
CREATE INDEX ix_ticket_category ON ticket (category);
CREATE INDEX ix_ticket_status_created_at ON ticket (status, created_at);
CREATE INDEX ix_comment_ticket_id ON ticket_comment (ticket_id);