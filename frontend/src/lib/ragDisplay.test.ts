import { describe, expect, it } from "vitest";
import { ragDisplayKind, ragPrimaryMessage, ragShowEscalation } from "@/lib/ragDisplay";
import type { RagQueryResponse } from "@/lib/types";

const base: RagQueryResponse = {
  outcome: "ANSWER",
  query: "q",
  answer: { text: "ok" },
  citations: [],
  confidence: { score: 0.9, basis: "RETRIEVAL" },
  escalation: { recommended: false, reason: null },
  error: null,
  noMatch: null,
};

describe("ragDisplay", () => {
  it("maps outcome to display kind", () => {
    expect(ragDisplayKind(null)).toBe("IDLE");
    expect(ragDisplayKind({ ...base, outcome: "NO_MATCH" })).toBe("NO_MATCH");
  });

  it("picks primary messages per outcome", () => {
    expect(ragPrimaryMessage(base)).toBe("ok");
    expect(
      ragPrimaryMessage({
        ...base,
        outcome: "ERROR",
        answer: null,
        error: { code: "GENERATION_FAILED", message: "Down" },
      }),
    ).toBe("Down");
  });

  it("shows escalation for no match", () => {
    expect(
      ragShowEscalation({
        ...base,
        outcome: "NO_MATCH",
        answer: null,
        noMatch: { code: "NO_RELEVANT_TICKETS", message: "None" },
        escalation: { recommended: true, reason: null },
      }),
    ).toBe(true);
  });
});