"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { ApiError, ticketsApi } from "@/lib/api";
import { TICKET_PRIORITIES } from "@/lib/ticketFilterValues";
import type { TicketPriority } from "@/lib/types";

export function TicketCreateForm() {
  const router = useRouter();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState<TicketPriority>("MEDIUM");
  const [assignee, setAssignee] = useState("");
  const [category, setCategory] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setFormError(null);
    setFieldErrors({});
    setSubmitting(true);
    try {
      const ticket = await ticketsApi.create({
        title: title.trim(),
        description: description.trim() || undefined,
        priority,
        assignee: assignee.trim() || undefined,
        category: category.trim() || undefined,
      });
      router.push(`/tickets/${ticket.ticketId}`);
    } catch (error) {
      if (error instanceof ApiError) {
        setFormError(error.message);
        const next: Record<string, string> = {};
        for (const detail of error.details) {
          next[detail.field] = detail.message;
        }
        setFieldErrors(next);
      } else {
        setFormError("Failed to create ticket.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="panel">
      <h1>Create ticket</h1>
      <form onSubmit={handleSubmit}>
        {formError ? <div className="banner-error">{formError}</div> : null}
        <label htmlFor="title">Title *</label>
        <input
          id="title"
          value={title}
          required
          onChange={(event) => setTitle(event.target.value)}
        />
        {fieldErrors.title ? (
          <div className="field-error">{fieldErrors.title}</div>
        ) : null}
        <label htmlFor="description">Description</label>
        <textarea
          id="description"
          value={description}
          onChange={(event) => setDescription(event.target.value)}
        />
        <label htmlFor="priority">Priority</label>
        <select
          id="priority"
          value={priority}
          onChange={(event) => setPriority(event.target.value as TicketPriority)}
        >
          {TICKET_PRIORITIES.map((item) => (
            <option key={item} value={item}>
              {item}
            </option>
          ))}
        </select>
        <label htmlFor="assignee">Assignee</label>
        <input
          id="assignee"
          value={assignee}
          onChange={(event) => setAssignee(event.target.value)}
        />
        <label htmlFor="category">Category</label>
        <input
          id="category"
          value={category}
          onChange={(event) => setCategory(event.target.value)}
        />
        <button className="primary" type="submit" disabled={submitting}>
          {submitting ? "Creating…" : "Create"}
        </button>
      </form>
    </section>
  );
}