import { describe, expect, it } from "vitest";
import {
  formatStatusLabel,
  TICKET_PRIORITIES,
  TICKET_STATUSES,
} from "@/lib/ticketFilterValues";

describe("ticketFilterValues", () => {
  it("exposes all ticket statuses", () => {
    expect(TICKET_STATUSES).toEqual([
      "OPEN",
      "IN_PROGRESS",
      "RESOLVED",
      "CLOSED",
      "CANCELLED",
    ]);
  });

  it("exposes priorities", () => {
    expect(TICKET_PRIORITIES).toEqual(["LOW", "MEDIUM", "HIGH"]);
  });

  it("formats status labels for display", () => {
    expect(formatStatusLabel("IN_PROGRESS")).toBe("IN PROGRESS");
  });
});