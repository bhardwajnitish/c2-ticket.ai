"use client";

import Link from "next/link";
import { useState } from "react";
import { ApiError, ragApi } from "@/lib/api";
import {
  ragDisplayKind,
  ragPrimaryMessage,
  ragShowEscalation,
  type RagDisplayKind,
} from "@/lib/ragDisplay";
import type { RagQueryResponse } from "@/lib/types";

export function RagAssistant() {
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(false);
  const [response, setResponse] = useState<RagQueryResponse | null>(null);
  const [clientError, setClientError] = useState<string | null>(null);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    const trimmed = query.trim();
    if (!trimmed) {
      setClientError("Enter a question before submitting.");
      return;
    }
    setClientError(null);
    setLoading(true);
    try {
      const result = await ragApi.query(trimmed);
      setResponse(result);
    } catch (error) {
      if (error instanceof ApiError) {
        setClientError(error.message);
      } else {
        setClientError("Unable to reach the server.");
      }
      setResponse(null);
    } finally {
      setLoading(false);
    }
  }

  const kind = ragDisplayKind(response);

  function panelClass(display: RagDisplayKind): string {
    if (display === "ANSWER") return "rag-panel--answer";
    if (display === "NO_MATCH") return "rag-panel--no-match";
    if (display === "ERROR") return "rag-panel--error";
    return "";
  }

  return (
    <section className="panel">
      <h1>Ask</h1>
      <p className="muted">Search support tickets with natural language.</p>
      <form onSubmit={handleSubmit}>
        <label htmlFor="rag-query">Your question</label>
        <textarea
          id="rag-query"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="e.g. How was TKT-1002 resolved?"
        />
        <button className="primary" type="submit" disabled={loading}>
          {loading ? "Searching…" : "Ask"}
        </button>
      </form>
      {clientError ? <div className="banner-error">{clientError}</div> : null}
      {response ? (
        <div
          className={`rag-result ${panelClass(kind)}`}
          data-outcome={response.outcome}
        >
          {kind === "ANSWER" ? (
            <>
              <h2>Answer</h2>
              <p>{ragPrimaryMessage(response)}</p>
              {response.confidence ? (
                <p className="muted">
                  Confidence: {response.confidence.score.toFixed(2)} (
                  {response.confidence.basis})
                </p>
              ) : null}
              {response.citations.length > 0 ? (
                <div>
                  <h3>Citations</h3>
                  <ul>
                    {response.citations.map((citation) => (
                      <li key={citation.ticketId}>
                        <Link href={`/tickets/${citation.ticketId}`}>
                          {citation.ticketId}
                        </Link>
                        {" — "}
                        {citation.title}
                      </li>
                    ))}
                  </ul>
                </div>
              ) : null}
            </>
          ) : null}
          {kind === "NO_MATCH" ? (
            <>
              <h2>No match</h2>
              <p>{response.noMatch?.message}</p>
              <p className="muted">Code: {response.noMatch?.code}</p>
              {ragShowEscalation(response) ? (
                <p>
                  {response.escalation?.reason ??
                    "Try rephrasing your question or contact support."}
                </p>
              ) : null}
            </>
          ) : null}
          {kind === "ERROR" ? (
            <>
              <h2>Error</h2>
              <p>{response.error?.message}</p>
              <p className="muted">Code: {response.error?.code}</p>
              <p>Please retry in a moment.</p>
            </>
          ) : null}
        </div>
      ) : null}
    </section>
  );
}