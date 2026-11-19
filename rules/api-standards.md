# API Standards — Implementation Rules

**Type:** Checklist  
**Normative contracts:** [spec/api-contract.md](../spec/api-contract.md), [spec/rag-api-contract.md](../spec/rag-api-contract.md), [spec/state-machine.md](../spec/state-machine.md)

---

## Global

- [ ] Base path **`/api/v1`** for all public REST endpoints
- [ ] **JSON** bodies and responses; property names **camelCase**
- [ ] **Unknown JSON properties rejected** with **400** and `error.code` **`VALIDATION_ERROR`** (ticket/comment endpoints)
- [ ] v1: **no auth**, **no pagination**, **no delete** endpoints

---

## Ticket error envelope (all ticket + comment errors)

```json
{
  "error": {
    "code": "string",
    "message": "string",
    "details": [ { "field": "string", "message": "string" } ]
  }
}
```

- [ ] **`details`** always present (empty array when none)
- [ ] Validation failures use **`VALIDATION_ERROR`**
- [ ] Not found: **`TICKET_NOT_FOUND`** + **404**
- [ ] Illegal transition: **`INVALID_STATUS_TRANSITION`** + **400** ([state-machine.md](../spec/state-machine.md))
- [ ] `status` on general PATCH: **`STATUS_CHANGE_NOT_ALLOWED_ON_THIS_ENDPOINT`** + **400**

---

## Status changes (exclusive endpoint)

- [ ] **Only** `PATCH /api/v1/tickets/{ticketId}/status` may change `status`
- [ ] General `PATCH /api/v1/tickets/{ticketId}` **must reject** body containing `status` ([api-contract.md](../spec/api-contract.md) §4.4)
- [ ] Status body: `{ "status": "<TicketStatus>" }` only (unknown properties → `VALIDATION_ERROR`)
- [ ] Create `POST /tickets`: reject client **`ticketId`** and **`status`** with **400**
- [ ] Server sets **`OPEN`**, generated **`ticketId`**, default priority **`MEDIUM`**

---

## Ticket representation

- [ ] Response fields: `ticketId`, `title`, `description`, `priority`, `status`, `assignee`, `category`, `resolutionNotes`, `createdAt`, `updatedAt`
- [ ] **Never** serialize internal numeric `id`

---

## List & filters

- [ ] `GET /api/v1/tickets` — query params **only**: `status`, `priority`, `assignee`, `category`
- [ ] Filters combined with **AND**
- [ ] **No** keyword / search query parameter
- [ ] Return **full** matching array (no pagination)

---

## Comments

- [ ] `POST /api/v1/tickets/{ticketId}/comments` → **201**; `id` is **UUID string**
- [ ] `GET .../comments` — chronological **`createdAt` ascending**

---

## RAG endpoint (separate envelope)

- [ ] `POST /api/v1/rag/query` — request `{ "query": string }`; blank after trim → **400**
- [ ] Response: [rag-api-contract.md](../spec/rag-api-contract.md) — **all top-level keys always present**
- [ ] Do **not** use ticket `error` envelope for successful RAG pipeline responses (validation **400** on RAG may differ per contract)

---

## HTTP status quick reference

| Case | HTTP |
|------|------|
| Ticket validation | 400 |
| `INVALID_STATUS_TRANSITION` | 400 |
| `TICKET_NOT_FOUND` | 404 |
| Ticket create | 201 |
| RAG `ANSWER` / `NO_MATCH` | 200 |
| RAG infrastructure `ERROR` | 503 (per rag-api-contract) |

---

## Sign-off

- [ ] MockMvc / contract tests assert codes and envelopes match this checklist
- [ ] [commands/review-code.md](../commands/review-code.md) API section completed