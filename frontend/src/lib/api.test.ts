import { describe, expect, it } from "vitest";
import { ApiError, parseApiError } from "@/lib/api";

describe("parseApiError", () => {
  it("maps ticket error envelope", () => {
    const error = parseApiError(400, {
      error: {
        code: "VALIDATION_ERROR",
        message: "Request validation failed.",
        details: [{ field: "title", message: "must not be blank" }],
      },
    });
    expect(error).toBeInstanceOf(ApiError);
    expect(error.status).toBe(400);
    expect(error.code).toBe("VALIDATION_ERROR");
    expect(error.details[0].field).toBe("title");
  });

  it("falls back when body missing", () => {
    const error = parseApiError(500, null);
    expect(error.code).toBe("UNKNOWN_ERROR");
    expect(error.message).toContain("500");
  });
});