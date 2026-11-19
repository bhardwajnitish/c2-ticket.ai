import type { Ticket, TicketListFilters } from "@/lib/types";

export function buildTicketListQuery(filters: TicketListFilters): string {
  const params = new URLSearchParams();
  if (filters.status) {
    params.set("status", filters.status);
  }
  if (filters.priority) {
    params.set("priority", filters.priority);
  }
  if (filters.assignee?.trim()) {
    params.set("assignee", filters.assignee.trim());
  }
  if (filters.category?.trim()) {
    params.set("category", filters.category.trim());
  }
  const qs = params.toString();
  return qs ? `?${qs}` : "";
}

export function applyKeywordFilter(tickets: Ticket[], keyword: string): Ticket[] {
  const trimmed = keyword.trim().toLowerCase();
  if (!trimmed) {
    return tickets;
  }
  return tickets.filter((ticket) => {
    const title = ticket.title?.toLowerCase() ?? "";
    const description = ticket.description?.toLowerCase() ?? "";
    return title.includes(trimmed) || description.includes(trimmed);
  });
}