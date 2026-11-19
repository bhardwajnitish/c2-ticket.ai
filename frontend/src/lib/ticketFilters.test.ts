import { describe, expect, it } from "vitest";
import { applyKeywordFilter, buildTicketListQuery } from "@/lib/ticketFilters";
import type { Ticket } from "@/lib/types";

const sample: Ticket[] = [
  {
    ticketId: "TKT-1001",
    title: "Password reset",
    description: "User cannot log in",
    priority: "HIGH",
    status: "OPEN",
    assignee: "alice",
    category: "auth",
    resolutionNotes: null,
    createdAt: "2026-01-01T00:00:00Z",
    updatedAt: "2026-01-01T00:00:00Z",
  },
  {
    ticketId: "TKT-1002",
    title: "Billing export",
    description: "CSV missing totals",
    priority: "LOW",
    status: "CLOSED",
    assignee: null,
    category: "billing",
    resolutionNotes: null,
    createdAt: "2026-01-02T00:00:00Z",
    updatedAt: "2026-01-02T00:00:00Z",
  },
];

describe("buildTicketListQuery", () => {
  it("includes only set filters", () => {
    expect(
      buildTicketListQuery({
        status: "OPEN",
        priority: "HIGH",
        assignee: "",
        category: "auth",
      }),
    ).toBe("?status=OPEN&priority=HIGH&category=auth");
  });

  it("returns empty string when no filters", () => {
    expect(buildTicketListQuery({})).toBe("");
  });
});

describe("applyKeywordFilter", () => {
  it("filters title and description client-side", () => {
    expect(applyKeywordFilter(sample, "billing")).toHaveLength(1);
    expect(applyKeywordFilter(sample, "log in")).toHaveLength(1);
  });

  it("returns all tickets when keyword blank", () => {
    expect(applyKeywordFilter(sample, "  ")).toHaveLength(2);
  });
});