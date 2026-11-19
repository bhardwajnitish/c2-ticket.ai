import type {
  ApiErrorBody,
  ApiErrorDetail,
  Comment,
  CreateTicketInput,
  RagQueryResponse,
  Ticket,
  TicketListFilters,
  TicketPriority,
  TicketStatus,
  UpdateTicketInput,
} from "@/lib/types";

const DEFAULT_BASE = "http://localhost:8080/api/v1";

export function getApiBaseUrl(): string {
  return (
    process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ?? DEFAULT_BASE
  );
}

export class ApiError extends Error {
  readonly status: number;
  readonly code: string;
  readonly details: ApiErrorDetail[];

  constructor(
    status: number,
    code: string,
    message: string,
    details: ApiErrorDetail[] = [],
  ) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export function parseApiError(
  status: number,
  payload: unknown,
): ApiError {
  const body = payload as { error?: Partial<ApiErrorBody> } | null;
  const error = body?.error;
  const code = error?.code ?? "UNKNOWN_ERROR";
  const message = error?.message ?? `Request failed with status ${status}`;
  const details = Array.isArray(error?.details) ? error.details : [];
  return new ApiError(status, code, message, details);
}

async function parseJsonSafe(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text) as unknown;
  } catch {
    return null;
  }
}

async function request<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  const url = `${getApiBaseUrl()}${path.startsWith("/") ? path : `/${path}`}`;
  const response = await fetch(url, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
  });
  const payload = await parseJsonSafe(response);
  if (!response.ok) {
    throw parseApiError(response.status, payload);
  }
  return payload as T;
}

function filterQuery(params: TicketListFilters): string {
  const search = new URLSearchParams();
  if (params.status) {
    search.set("status", params.status);
  }
  if (params.priority) {
    search.set("priority", params.priority);
  }
  if (params.assignee?.trim()) {
    search.set("assignee", params.assignee.trim());
  }
  if (params.category?.trim()) {
    search.set("category", params.category.trim());
  }
  const qs = search.toString();
  return qs ? `?${qs}` : "";
}

export const ticketsApi = {
  list(filters: TicketListFilters = {}): Promise<Ticket[]> {
    return request<Ticket[]>(`/tickets${filterQuery(filters)}`);
  },

  get(ticketId: string): Promise<Ticket> {
    return request<Ticket>(`/tickets/${encodeURIComponent(ticketId)}`);
  },

  create(input: CreateTicketInput): Promise<Ticket> {
    return request<Ticket>("/tickets", {
      method: "POST",
      body: JSON.stringify(input),
    });
  },

  update(ticketId: string, input: UpdateTicketInput): Promise<Ticket> {
    return request<Ticket>(`/tickets/${encodeURIComponent(ticketId)}`, {
      method: "PATCH",
      body: JSON.stringify(input),
    });
  },

  transitionStatus(ticketId: string, status: TicketStatus): Promise<Ticket> {
    return request<Ticket>(
      `/tickets/${encodeURIComponent(ticketId)}/status`,
      {
        method: "PATCH",
        body: JSON.stringify({ status }),
      },
    );
  },
};

export const commentsApi = {
  list(ticketId: string): Promise<Comment[]> {
    return request<Comment[]>(
      `/tickets/${encodeURIComponent(ticketId)}/comments`,
    );
  },

  create(
    ticketId: string,
    input: { content: string; author?: string },
  ): Promise<Comment> {
    return request<Comment>(
      `/tickets/${encodeURIComponent(ticketId)}/comments`,
      {
        method: "POST",
        body: JSON.stringify(input),
      },
    );
  },
};

export const ragApi = {
  async query(query: string): Promise<RagQueryResponse> {
    const url = `${getApiBaseUrl()}/rag/query`;
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ query }),
    });
    const payload = await parseJsonSafe(response);
    if (response.ok || response.status === 503) {
      return payload as RagQueryResponse;
    }
    throw parseApiError(response.status, payload);
  },
};

export type { TicketPriority };