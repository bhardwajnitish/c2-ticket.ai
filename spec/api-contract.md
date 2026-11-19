# c2-ticket.ai — REST API Contract (Tickets & Comments)

**Sources:** [data-model.md](data-model.md), [state-machine.md](state-machine.md)  
**Related:** [rag-api-contract.md](rag-api-contract.md) (separate envelope for RAG query)  
**Document type:** Stable HTTP/JSON contract (no implementation code)  
**Version:** v1

---

## 1. Conventions

| Rule | Value |
|------|--------|
| Base path | **`/api/v1`** |
| Format | **JSON** |
| Property names | **camelCase** |
| Authentication | **None** in v1 |
| Pagination | **None** in v1 — list endpoints return the **full** matching array |
| Delete | **Not supported** in v1 |
| Unknown JSON properties | **Reject** request with **400** and `VALIDATION_ERROR` |
| Timestamps | ISO-8601 strings in UTC, e.g. `2026-11-19T09:30:00Z` |
| Enums | Serialized as strings (`OPEN`, `MEDIUM`, …) |

**Internal surrogate `id`:** Never exposed. Public key is **`ticketId`** only.

---

## 2. Shared error envelope

All **ticket and comment** endpoints below use this body on error responses (4xx/5xx as specified per endpoint).

```json
{
  "error": {
    "code": "string",
    "message": "string",
    "details": [
      { "field": "string", "message": "string" }
    ]
  }
}
```

| Field | Rules |
|-------|--------|
| `code` | Stable machine-readable code (see §9) |
| `message` | Human-readable summary |
| `details` | Always present; **empty array `[]`** when no field-level issues |

**Validation failures** use **`code`: `VALIDATION_ERROR`**. Each violated field appears in `details` with `field` (JSON path or logical name) and `message`.

**Examples:** unknown property → `VALIDATION_ERROR` with detail for that property; title over 255 chars → detail on `title`.

---

## 3. Types

### 3.1 `TicketStatus`

`OPEN` | `IN_PROGRESS` | `RESOLVED` | `CLOSED` | `CANCELLED`

Per [data-model.md](data-model.md) §2.2. New tickets are **`OPEN`**.

### 3.2 `TicketPriority`

`LOW` | `MEDIUM` | `HIGH`

Default **`MEDIUM`** on create when omitted ([data-model.md](data-model.md) §2.3).

### 3.3 `Ticket` (response)

| Field | Type | Null in JSON? | Constraints |
|-------|------|---------------|-------------|
| `ticketId` | string | No | `^TKT-[0-9]+$` |
| `title` | string | No | max **255** |
| `description` | string | Yes | max **10 000** |
| `priority` | `TicketPriority` | No | |
| `status` | `TicketStatus` | No | |
| `assignee` | string | Yes | |
| `category` | string | Yes | |
| `resolutionNotes` | string | Yes | max **10 000** |
| `createdAt` | string (date-time) | No | |
| `updatedAt` | string (date-time) | No | |

### 3.4 `Comment` (response)

| Field | Type | Null in JSON? | Constraints |
|-------|------|---------------|-------------|
| `id` | string | No | **UUID** string |
| `ticketId` | string | No | `^TKT-[0-9]+$` |
| `author` | string | Yes | |
| `content` | string | No | |
| `createdAt` | string (date-time) | No | |

---

## 4. Endpoints

### 4.1 Create ticket

| Item | Value |
|------|--------|
| Method / path | **`POST /api/v1/tickets`** |
| Success | **201 Created** — body is a `Ticket` |

**Request body** (only these properties allowed):

| Field | Required | Rules |
|-------|----------|--------|
| `title` | **Yes** | Non-blank after trim; max 255 |
| `description` | No | Max 10 000 |
| `priority` | No | `TicketPriority`; default **`MEDIUM`** if omitted |
| `assignee` | No | |
| `category` | No | |

**Rejected on create (400, `VALIDATION_ERROR`):**

| Condition | Detail |
|-----------|--------|
| Client sends **`ticketId`** | Field not allowed |
| Client sends **`status`** | Field not allowed |
| Unknown properties | Unknown property |
| Missing/blank `title` | `title` |
| Invalid enum value | respective field |

**Server behavior:**

| Field | Set by server |
|-------|----------------|
| `ticketId` | Generated (`TKT-1001`, …) per [data-model.md](data-model.md) |
| `status` | **`OPEN`** |
| `priority` | **`MEDIUM`** if omitted |
| `resolutionNotes` | **`null`** |
| `createdAt`, `updatedAt` | Server clock |

---

### 4.2 List tickets

| Item | Value |
|------|--------|
| Method / path | **`GET /api/v1/tickets`** |
| Success | **200 OK** — body is a **JSON array** of `Ticket` (may be empty) |

**Query parameters** (all optional; **only** these four):

| Parameter | Type | Match |
|-----------|------|--------|
| `status` | `TicketStatus` | Exact |
| `priority` | `TicketPriority` | Exact |
| `assignee` | string | Exact |
| `category` | string | Exact |

| Rule | Detail |
|------|--------|
| Combination | **AND** — ticket must satisfy every supplied filter |
| Omitted parameter | No constraint on that dimension |
| Keyword search | **Not supported** — no `q`, `keyword`, or full-text parameter in v1 |
| Pagination | **None** — entire result set |

Invalid enum in query → **400** `VALIDATION_ERROR` on the query parameter name.

**Sort order:** Not specified in v1 (**open** for implementation; recommend `createdAt` descending documented in release notes).

---

### 4.3 Get ticket

| Item | Value |
|------|--------|
| Method / path | **`GET /api/v1/tickets/{ticketId}`** |
| Path `ticketId` | Business key |

| Outcome | HTTP | Body |
|---------|------|------|
| Found | **200** | `Ticket` |
| Not found | **404** | Error envelope; `code`: **`TICKET_NOT_FOUND`** |

Malformed `ticketId` path (fails `^TKT-[0-9]+$`) → **404** `TICKET_NOT_FOUND` (treat as not found for v1).

---

### 4.4 Partial update ticket

| Item | Value |
|------|--------|
| Method / path | **`PATCH /api/v1/tickets/{ticketId}`** |
| Success | **200 OK** — body is updated `Ticket` |

**Allowed body fields** (each optional; at least one required):

| Field | Rules |
|-------|--------|
| `title` | Non-blank if present; max 255 |
| `description` | Max 10 000; may be `null` to clear |
| `priority` | Valid enum |
| `assignee` | May be `null` to clear |
| `category` | May be `null` to clear |
| `resolutionNotes` | Max 10 000; may be `null` to clear |

**Forbidden:**

| Condition | HTTP | `code` |
|-----------|------|--------|
| Body contains **`status`** | **400** | **`STATUS_CHANGE_NOT_ALLOWED_ON_THIS_ENDPOINT`** |
| Unknown properties | **400** | `VALIDATION_ERROR` |
| Empty body (no allowed fields) | **400** | `VALIDATION_ERROR` |

| Outcome | HTTP |
|---------|------|
| Ticket not found | **404** `TICKET_NOT_FOUND` |
| Validation failure | **400** `VALIDATION_ERROR` |

**Semantics:** PATCH applies only sent fields. Omitted fields are unchanged. Explicit JSON `null` clears nullable fields where allowed above.

**Does not change:** `ticketId`, `status`, `createdAt`. **`updatedAt`** set on successful write.

---

### 4.5 Transition ticket status

Per [state-machine.md](state-machine.md) §4.1 — **only** path that mutates `status`.

| Item | Value |
|------|--------|
| Method / path | **`PATCH /api/v1/tickets/{ticketId}/status`** |
| Success | **200 OK** — body is updated `Ticket` |

**Request body:**

```json
{
  "status": "IN_PROGRESS"
}
```

| Field | Required |
|-------|----------|
| `status` | **Yes** — target `TicketStatus` |

| Condition | HTTP | `error.code` |
|-----------|------|----------------|
| Unknown properties | **400** | `VALIDATION_ERROR` |
| Illegal transition (policy reject) | **400** | **`INVALID_STATUS_TRANSITION`** |
| Same-status | **400** | **`INVALID_STATUS_TRANSITION`** |
| From terminal state | **400** | **`INVALID_STATUS_TRANSITION`** |
| Ticket not found | **404** | `TICKET_NOT_FOUND` |
| Invalid/missing `status` in body | **400** | `VALIDATION_ERROR` |

On **`INVALID_STATUS_TRANSITION`**, the ticket row is **unchanged** ([state-machine.md](state-machine.md) §5).

`details` may be **`[]`** for `INVALID_STATUS_TRANSITION` or include optional `from`/`to` in a later revision; v1 allows empty `details`.

---

### 4.6 Add comment

| Item | Value |
|------|--------|
| Method / path | **`POST /api/v1/tickets/{ticketId}/comments`** |
| Success | **201 Created** — body is `Comment` |

**Request body:**

| Field | Required |
|-------|----------|
| `author` | No |
| `content` | **Yes** — non-blank after trim |

| Rule | Detail |
|------|--------|
| Unknown properties | **400** `VALIDATION_ERROR` |
| Parent missing | **404** `TICKET_NOT_FOUND` |
| `id`, `ticketId`, `createdAt` in body | **400** — client must not set |

**Server sets:** `id` (UUID string), `ticketId` (path), `createdAt`.

---

### 4.7 List comments

| Item | Value |
|------|--------|
| Method / path | **`GET /api/v1/tickets/{ticketId}/comments`** |
| Success | **200 OK** — JSON array of `Comment` |

| Rule | Detail |
|------|--------|
| Order | **Chronological** by `createdAt` ascending ([data-model.md](data-model.md)) |
| Pagination | **None** — full array |
| Parent missing | **404** `TICKET_NOT_FOUND` |

---

## 5. Cross-cutting validation

| Rule | HTTP | Code |
|------|------|------|
| `Content-Type` not JSON where body expected | **415** or **400** | `VALIDATION_ERROR` (implementation choice; prefer **415** when unsupported media type) |
| Malformed JSON | **400** | `VALIDATION_ERROR` |
| String length over data-model max | **400** | `VALIDATION_ERROR` with `details` |

---

## 6. Side effects (informational)

Successful **create**, **PATCH** ticket, **PATCH** status, and **POST** comment trigger **RAG re-ingest** for the ticket per [rag-ingestion.md](rag-ingestion.md). Ingest failures do not roll back the API mutation. Not reflected in HTTP response v1.

---

## 7. Examples

### 7.1 Create ticket (201)

**Request:**

```http
POST /api/v1/tickets
Content-Type: application/json

{
  "title": "Login failure",
  "description": "Cannot sign in",
  "priority": "HIGH"
}
```

**Response:**

```json
{
  "ticketId": "TKT-1001",
  "title": "Login failure",
  "description": "Cannot sign in",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": null,
  "category": null,
  "resolutionNotes": null,
  "createdAt": "2026-11-19T09:00:00Z",
  "updatedAt": "2026-11-19T09:00:00Z"
}
```

### 7.2 Validation error (400)

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed.",
    "details": [
      { "field": "ticketId", "message": "Field is not allowed on create." }
    ]
  }
}
```

### 7.3 Invalid transition (400)

```json
{
  "error": {
    "code": "INVALID_STATUS_TRANSITION",
    "message": "Status transition is not allowed.",
    "details": []
  }
}
```

### 7.4 Not found (404)

```json
{
  "error": {
    "code": "TICKET_NOT_FOUND",
    "message": "Ticket not found.",
    "details": []
  }
}
```

---

## 8. Endpoint summary

| Method | Path | Success |
|--------|------|---------|
| `POST` | `/api/v1/tickets` | **201** `Ticket` |
| `GET` | `/api/v1/tickets` | **200** `Ticket[]` |
| `GET` | `/api/v1/tickets/{ticketId}` | **200** `Ticket` |
| `PATCH` | `/api/v1/tickets/{ticketId}` | **200** `Ticket` |
| `PATCH` | `/api/v1/tickets/{ticketId}/status` | **200** `Ticket` |
| `POST` | `/api/v1/tickets/{ticketId}/comments` | **201** `Comment` |
| `GET` | `/api/v1/tickets/{ticketId}/comments` | **200** `Comment[]` |

---

## 9. Stable error codes (v1)

| Code | Typical HTTP |
|------|----------------|
| `VALIDATION_ERROR` | 400 |
| `TICKET_NOT_FOUND` | 404 |
| `INVALID_STATUS_TRANSITION` | 400 |
| `STATUS_CHANGE_NOT_ALLOWED_ON_THIS_ENDPOINT` | 400 |

Server **5xx** responses should use a generic code (e.g. `INTERNAL_ERROR`) — not required for v1 client logic beyond retry/display.

---

## 10. Out of scope for this document

| Topic | Where |
|-------|--------|
| `POST /api/v1/rag/query` | [rag-api-contract.md](rag-api-contract.md) |
| Auth, pagination, delete | Later versions |
| Sort order for `GET /tickets` | v1 open |

---

## Document control

| Field | Value |
|-------|--------|
| Version | 1.0 |
| Status | Locked for frontend/backend implementation |
| Depends on | data-model, state-machine |