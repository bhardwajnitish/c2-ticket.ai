import type { RagQueryResponse } from "@/lib/types";

export type RagDisplayKind = "ANSWER" | "NO_MATCH" | "ERROR" | "IDLE";

export function ragDisplayKind(
  response: RagQueryResponse | null,
): RagDisplayKind {
  if (!response) {
    return "IDLE";
  }
  return response.outcome;
}

export function ragPrimaryMessage(response: RagQueryResponse): string | null {
  if (response.outcome === "ANSWER") {
    return response.answer?.text ?? null;
  }
  if (response.outcome === "NO_MATCH") {
    return response.noMatch?.message ?? "No matching tickets.";
  }
  if (response.outcome === "ERROR") {
    return response.error?.message ?? "Something went wrong.";
  }
  return null;
}

export function ragShowEscalation(response: RagQueryResponse): boolean {
  return response.escalation?.recommended === true;
}