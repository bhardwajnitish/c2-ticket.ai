"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useMemo, useState } from "react";
import { ApiError, ticketsApi } from "@/lib/api";
import { applyKeywordFilter } from "@/lib/ticketFilters";
import {
  formatStatusLabel,
  TICKET_PRIORITIES,
  TICKET_STATUSES,
} from "@/lib/ticketFilterValues";
import type { Ticket, TicketListFilters, TicketPriority, TicketStatus } from "@/lib/types";

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString();
}

export function TicketListView() {
  const router = useRouter();
  const [filters, setFilters] = useState<TicketListFilters>({
    status: "",
    priority: "",
    assignee: "",
    category: "",
  });
  const [draftFilters, setDraftFilters] = useState(filters);
  const [keyword, setKeyword] = useState("");
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTickets = useCallback(async (active: TicketListFilters) => {
    setLoading(true);
    setError(null);
    try {
      const data = await ticketsApi.list(active);
      setTickets(data);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to load tickets.");
      setTickets([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void fetchTickets(filters);
  }, [filters, fetchTickets]);

  const visibleTickets = useMemo(
    () => applyKeywordFilter(tickets, keyword),
    [tickets, keyword],
  );

  function applyFilters() {
    setFilters({
      status: draftFilters.status,
      priority: draftFilters.priority,
      assignee: draftFilters.assignee?.trim() ?? "",
      category: draftFilters.category?.trim() ?? "",
    });
  }

  function clearFilters() {
    const empty: TicketListFilters = {
      status: "",
      priority: "",
      assignee: "",
      category: "",
    };
    setDraftFilters(empty);
    setFilters(empty);
  }

  return (
    <section className="panel">
      <h1>Tickets</h1>
      <div className="actions-row">
        <Link className="button primary" href="/tickets/new">
          Create ticket
        </Link>
        <Link className="button" href="/ask">
          Ask
        </Link>
      </div>
      <p className="muted">Server filters (AND). Keyword search is client-side on title and description.</p>
      <div className="filters">
        <div>
          <label htmlFor="filter-status">Status</label>
          <select
            id="filter-status"
            value={draftFilters.status ?? ""}
            onChange={(event) =>
              setDraftFilters((prev) => ({
                ...prev,
                status: event.target.value as TicketStatus | "",
              }))
            }
          >
            <option value="">Any</option>
            {TICKET_STATUSES.map((status) => (
              <option key={status} value={status}>
                {formatStatusLabel(status)}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="filter-priority">Priority</label>
          <select
            id="filter-priority"
            value={draftFilters.priority ?? ""}
            onChange={(event) =>
              setDraftFilters((prev) => ({
                ...prev,
                priority: event.target.value as TicketPriority | "",
              }))
            }
          >
            <option value="">Any</option>
            {TICKET_PRIORITIES.map((priority) => (
              <option key={priority} value={priority}>
                {priority}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="filter-assignee">Assignee</label>
          <input
            id="filter-assignee"
            value={draftFilters.assignee ?? ""}
            onChange={(event) =>
              setDraftFilters((prev) => ({ ...prev, assignee: event.target.value }))
            }
          />
        </div>
        <div>
          <label htmlFor="filter-category">Category</label>
          <input
            id="filter-category"
            value={draftFilters.category ?? ""}
            onChange={(event) =>
              setDraftFilters((prev) => ({ ...prev, category: event.target.value }))
            }
          />
        </div>
        <div>
          <label htmlFor="filter-keyword">Keyword</label>
          <input
            id="filter-keyword"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="Title or description"
          />
        </div>
      </div>
      <div className="actions-row">
        <button className="primary" type="button" onClick={applyFilters}>
          Apply filters
        </button>
        <button type="button" onClick={clearFilters}>
          Clear
        </button>
      </div>
      {error ? <div className="banner-error">{error}</div> : null}
      {loading ? <p>Loading…</p> : null}
      {!loading ? (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Ticket ID</th>
                <th>Title</th>
                <th>Status</th>
                <th>Priority</th>
                <th>Assignee</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {visibleTickets.map((ticket) => (
                <tr
                  key={ticket.ticketId}
                  onClick={() => router.push(`/tickets/${ticket.ticketId}`)}
                >
                  <td>{ticket.ticketId}</td>
                  <td>{ticket.title}</td>
                  <td>{formatStatusLabel(ticket.status)}</td>
                  <td>{ticket.priority}</td>
                  <td>{ticket.assignee ?? "—"}</td>
                  <td>{formatDate(ticket.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {visibleTickets.length === 0 ? (
            <p className="muted">No tickets to display.</p>
          ) : null}
        </div>
      ) : null}
    </section>
  );
}