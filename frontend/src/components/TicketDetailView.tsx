"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { ApiError, commentsApi, ticketsApi } from "@/lib/api";
import { formatStatusLabel, TICKET_PRIORITIES } from "@/lib/ticketFilterValues";
import {
  allowedTransitionsFrom,
  transitionButtonLabel,
} from "@/lib/statusTransitions";
import type { Comment, Ticket, TicketPriority, TicketStatus } from "@/lib/types";

function formatDate(iso: string): string {
  return new Date(iso).toLocaleString();
}

type Props = {
  ticketId: string;
};

export function TicketDetailView({ ticketId }: Props) {
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<TicketPriority>("MEDIUM");
  const [assignee, setAssignee] = useState("");
  const [category, setCategory] = useState("");
  const [resolutionNotes, setResolutionNotes] = useState("");
  const [notFound, setNotFound] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [commentAuthor, setCommentAuthor] = useState("");
  const [commentContent, setCommentContent] = useState("");
  const [statusError, setStatusError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setError(null);
    setNotFound(false);
    try {
      const [ticketData, commentData] = await Promise.all([
        ticketsApi.get(ticketId),
        commentsApi.list(ticketId),
      ]);
      setTicket(ticketData);
      setTitle(ticketData.title);
      setDescription(ticketData.description ?? "");
      setPriority(ticketData.priority);
      setAssignee(ticketData.assignee ?? "");
      setCategory(ticketData.category ?? "");
      setResolutionNotes(ticketData.resolutionNotes ?? "");
      setComments(commentData);
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) {
        setNotFound(true);
      } else {
        setError(err instanceof ApiError ? err.message : "Failed to load ticket.");
      }
    }
  }, [ticketId]);

  useEffect(() => {
    void load();
  }, [load]);

  async function saveFields(event: React.FormEvent) {
    event.preventDefault();
    setError(null);
    setFieldErrors({});
    try {
      const updated = await ticketsApi.update(ticketId, {
        title: title.trim(),
        description: description.trim() || null,
        priority,
        assignee: assignee.trim() || null,
        category: category.trim() || null,
        resolutionNotes: resolutionNotes.trim() || null,
      });
      setTicket(updated);
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
        const next: Record<string, string> = {};
        for (const detail of err.details) {
          next[detail.field] = detail.message;
        }
        setFieldErrors(next);
      } else {
        setError("Failed to save ticket.");
      }
    }
  }

  async function transitionTo(target: TicketStatus) {
    setStatusError(null);
    try {
      const updated = await ticketsApi.transitionStatus(ticketId, target);
      setTicket(updated);
    } catch (err) {
      setStatusError(
        err instanceof ApiError ? err.message : "Status change failed.",
      );
    }
  }

  async function addComment(event: React.FormEvent) {
    event.preventDefault();
    if (!commentContent.trim()) {
      return;
    }
    try {
      await commentsApi.create(ticketId, {
        content: commentContent.trim(),
        author: commentAuthor.trim() || undefined,
      });
      setCommentContent("");
      const refreshed = await commentsApi.list(ticketId);
      setComments(refreshed);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Failed to add comment.");
    }
  }

  if (notFound) {
    return (
      <section className="panel">
        <h1>Ticket not found</h1>
        <p>The ticket {ticketId} does not exist.</p>
        <Link href="/tickets">Back to list</Link>
      </section>
    );
  }

  if (!ticket) {
    return (
      <section className="panel">
        <p>{error ?? "Loading…"}</p>
      </section>
    );
  }

  const transitions = allowedTransitionsFrom(ticket.status);

  return (
    <section className="panel">
      <h1>{ticket.ticketId}</h1>
      <p className="muted">
        Status: {formatStatusLabel(ticket.status)} · Created {formatDate(ticket.createdAt)} ·
        Updated {formatDate(ticket.updatedAt)}
      </p>
      {error ? <div className="banner-error">{error}</div> : null}
      <form onSubmit={saveFields}>
        <label htmlFor="detail-title">Title</label>
        <input
          id="detail-title"
          value={title}
          onChange={(event) => setTitle(event.target.value)}
        />
        {fieldErrors.title ? (
          <div className="field-error">{fieldErrors.title}</div>
        ) : null}
        <label htmlFor="detail-description">Description</label>
        <textarea
          id="detail-description"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
        />
        <div className="detail-grid">
          <div>
            <label htmlFor="detail-priority">Priority</label>
            <select
              id="detail-priority"
              value={priority}
              onChange={(event) =>
                setPriority(event.target.value as TicketPriority)
              }
            >
              {TICKET_PRIORITIES.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="detail-assignee">Assignee</label>
            <input
              id="detail-assignee"
              value={assignee}
              onChange={(event) => setAssignee(event.target.value)}
            />
          </div>
        </div>
        <label htmlFor="detail-category">Category</label>
        <input
          id="detail-category"
          value={category}
          onChange={(event) => setCategory(event.target.value)}
        />
        <label htmlFor="detail-resolution">Resolution notes</label>
        <textarea
          id="detail-resolution"
          value={resolutionNotes}
          onChange={(event) => setResolutionNotes(event.target.value)}
        />
        <button className="primary" type="submit">
          Save changes
        </button>
      </form>
      <h2>Status actions</h2>
      {statusError ? <div className="banner-error">{statusError}</div> : null}
      <div className="actions-row">
        {transitions.map((target) => (
          <button key={target} type="button" onClick={() => void transitionTo(target)}>
            {transitionButtonLabel(target)}
          </button>
        ))}
        {transitions.length === 0 ? (
          <p className="muted">No further transitions from {ticket.status}.</p>
        ) : null}
      </div>
      <h2>Comments</h2>
      <ul className="comment-list">
        {comments.map((comment) => (
          <li key={comment.id}>
            <strong>{comment.author ?? "Anonymous"}</strong> ·{" "}
            <span className="muted">{formatDate(comment.createdAt)}</span>
            <p>{comment.content}</p>
          </li>
        ))}
      </ul>
      <form onSubmit={addComment}>
        <label htmlFor="comment-author">Author</label>
        <input
          id="comment-author"
          value={commentAuthor}
          onChange={(event) => setCommentAuthor(event.target.value)}
        />
        <label htmlFor="comment-content">Comment *</label>
        <textarea
          id="comment-content"
          required
          value={commentContent}
          onChange={(event) => setCommentContent(event.target.value)}
        />
        <button type="submit">Add comment</button>
      </form>
      <p>
        <Link href="/tickets">Back to list</Link>
      </p>
    </section>
  );
}