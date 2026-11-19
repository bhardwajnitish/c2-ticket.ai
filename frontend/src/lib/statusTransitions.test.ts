import { describe, expect, it } from "vitest";
import {
  allowedTransitionsFrom,
  isTransitionAllowed,
} from "@/lib/statusTransitions";

describe("statusTransitions", () => {
  it("matches backend graph for OPEN", () => {
    expect(allowedTransitionsFrom("OPEN")).toEqual([
      "IN_PROGRESS",
      "CANCELLED",
    ]);
  });

  it("allows RESOLVED to CLOSED only", () => {
    expect(isTransitionAllowed("RESOLVED", "CLOSED")).toBe(true);
    expect(isTransitionAllowed("RESOLVED", "OPEN")).toBe(false);
  });

  it("has no actions from terminal states", () => {
    expect(allowedTransitionsFrom("CLOSED")).toEqual([]);
    expect(allowedTransitionsFrom("CANCELLED")).toEqual([]);
  });
});