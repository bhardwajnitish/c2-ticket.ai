import type { TicketPriority, TicketStatus } from "@/lib/types";

export const TICKET_STATUSES: TicketStatus[] = [
  "OPEN",
  "IN_PROGRESS",
  "RESOLVED",
  "CLOSED",
  "CANCELLED",
];

export const TICKET_PRIORITIES: TicketPriority[] = ["LOW", "MEDIUM", "HIGH"];

export function formatStatusLabel(status: TicketStatus): string {
  return status.replaceAll("_", " ");
}