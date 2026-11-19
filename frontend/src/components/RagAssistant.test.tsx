import { cleanup, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, describe, expect, it, vi } from "vitest";
import { RagAssistant } from "@/components/RagAssistant";
import * as api from "@/lib/api";
import type { RagQueryResponse } from "@/lib/types";

describe("RagAssistant display states", () => {
  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it("renders ANSWER with citation links", async () => {
    const response: RagQueryResponse = {
      outcome: "ANSWER",
      query: "reset",
      answer: { text: "Links expire after 30 minutes." },
      citations: [{ ticketId: "TKT-1001", title: "Password reset" }],
      confidence: { score: 0.88, basis: "RETRIEVAL" },
      escalation: { recommended: false, reason: null },
      error: null,
      noMatch: null,
    };
    vi.spyOn(api.ragApi, "query").mockResolvedValue(response);

    const user = userEvent.setup();
    const view = render(<RagAssistant />);
    const form = view.container.querySelector("form");
    expect(form).toBeTruthy();
    await user.type(within(form as HTMLElement).getByLabelText(/your question/i), "How long?");
    await user.click(within(form as HTMLElement).getByRole("button", { name: "Ask" }));

    expect(await screen.findByText("Answer")).toBeInTheDocument();
    expect(screen.getByText("Links expire after 30 minutes.")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "TKT-1001" })).toHaveAttribute(
      "href",
      "/tickets/TKT-1001",
    );
  });

  it("renders NO_MATCH state without answer body", async () => {
    vi.spyOn(api.ragApi, "query").mockResolvedValue({
      outcome: "NO_MATCH",
      query: "mars",
      answer: null,
      citations: [],
      confidence: null,
      escalation: { recommended: true, reason: "Try broader terms." },
      error: null,
      noMatch: { code: "NO_RELEVANT_TICKETS", message: "No tickets matched." },
    });

    const user = userEvent.setup();
    const view = render(<RagAssistant />);
    const form = view.container.querySelector("form") as HTMLElement;
    await user.type(within(form).getByLabelText(/your question/i), "mars");
    await user.click(within(form).getByRole("button", { name: "Ask" }));

    expect(await screen.findByText("No match")).toBeInTheDocument();
    expect(screen.queryByText("Answer")).not.toBeInTheDocument();
    expect(screen.getByText("Try broader terms.")).toBeInTheDocument();
  });

  it("renders ERROR state distinctly", async () => {
    vi.spyOn(api.ragApi, "query").mockResolvedValue({
      outcome: "ERROR",
      query: "q",
      answer: null,
      citations: [],
      confidence: null,
      escalation: null,
      error: { code: "GENERATION_FAILED", message: "Unable to generate." },
      noMatch: null,
    });

    const user = userEvent.setup();
    const view = render(<RagAssistant />);
    const form = view.container.querySelector("form") as HTMLElement;
    await user.type(within(form).getByLabelText(/your question/i), "q");
    await user.click(within(form).getByRole("button", { name: "Ask" }));

    expect(await screen.findByText("Error")).toBeInTheDocument();
    expect(screen.getByText("Unable to generate.")).toBeInTheDocument();
    expect(screen.getByText(/GENERATION_FAILED/)).toBeInTheDocument();
  });
});