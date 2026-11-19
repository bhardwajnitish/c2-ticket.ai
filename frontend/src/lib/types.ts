export type TicketStatus =
  | "OPEN"
  | "IN_PROGRESS"
  | "RESOLVED"
  | "CLOSED"
  | "CANCELLED";

export type TicketPriority = "LOW" | "MEDIUM" | "HIGH";

export type Ticket = {
  ticketId: string;
  title: string;
  description: string | null;
  priority: TicketPriority;
  status: TicketStatus;
  assignee: string | null;
  category: string | null;
  resolutionNotes: string | null;
  createdAt: string;
  updatedAt: string;
};

export type Comment = {
  id: string;
  ticketId: string;
  author: string | null;
  content: string;
  createdAt: string;
};

export type TicketListFilters = {
  status?: TicketStatus | "";
  priority?: TicketPriority | "";
  assignee?: string;
  category?: string;
};

export type CreateTicketInput = {
  title: string;
  description?: string;
  priority?: TicketPriority;
  assignee?: string;
  category?: string;
};

export type UpdateTicketInput = {
  title?: string;
  description?: string | null;
  priority?: TicketPriority;
  assignee?: string | null;
  category?: string | null;
  resolutionNotes?: string | null;
};

export type RagQueryOutcome = "ANSWER" | "NO_MATCH" | "ERROR";

export type RagQueryResponse = {
  outcome: RagQueryOutcome;
  query: string;
  answer: { text: string } | null;
  citations: { ticketId: string; title: string }[];
  confidence: { score: number; basis: string } | null;
  escalation: { recommended: boolean; reason: string | null } | null;
  error: { code: string; message: string } | null;
  noMatch: { code: string; message: string } | null;
};

export type ApiErrorDetail = {
  field: string;
  message: string;
};

export type ApiErrorBody = {
  code: string;
  message: string;
  details: ApiErrorDetail[];
};