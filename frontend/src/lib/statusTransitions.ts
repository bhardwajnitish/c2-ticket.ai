import type { TicketStatus } from "@/lib/types";

const ALLOWED: Record<TicketStatus, TicketStatus[]> = {
  OPEN: ["IN_PROGRESS", "CANCELLED"],
  IN_PROGRESS: ["RESOLVED", "CANCELLED"],
  RESOLVED: ["CLOSED"],
  CLOSED: [],
  CANCELLED: [],
};

export function allowedTransitionsFrom(status: TicketStatus): TicketStatus[] {
  return [...ALLOWED[status]];
}

export function isTransitionAllowed(
  from: TicketStatus,
  to: TicketStatus,
): boolean {
  return ALLOWED[from].includes(to);
}

export function transitionButtonLabel(target: TicketStatus): string {
  return `Mark ${target.replaceAll("_", " ")}`;
}