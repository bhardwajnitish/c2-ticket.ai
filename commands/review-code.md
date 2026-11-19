# Command: Review Code

**Purpose:** Verify `src/` against `spec/` and `rules/`.  
**Prerequisite:** Specs reviewed ([review-spec.md](review-spec.md)) with no unresolved contradictions.

---

## Inputs

- Application source under `src/main/` and `src/test/`
- [rules/java-springboot.md](../rules/java-springboot.md)
- [rules/api-standards.md](../rules/api-standards.md)
- [rules/rag-vector-store.md](../rules/rag-vector-store.md)
- [rules/testing.md](../rules/testing.md)
- Relevant `spec/*.md`

---

## Procedure (checklist)

### Layering & packages

- [ ] Packages match `com.example.ticket.*`, `com.example.rag.*`, `com.example.config`
- [ ] Controllers in `*.api` — **no** business rules (grep for transition matrix, threshold logic)
- [ ] `TicketStatusTransitionPolicy` in `ticket.domain` — sole transition matrix
- [ ] Domain module does not depend on web/JPA

### API standards

- [ ] Paths under `/api/v1`
- [ ] Ticket errors use `{ error: { code, message, details } }`
- [ ] Unknown JSON properties rejected (`VALIDATION_ERROR`)
- [ ] `status` only on `PATCH .../status`
- [ ] Stable codes: `TICKET_NOT_FOUND`, `INVALID_STATUS_TRANSITION`, `STATUS_CHANGE_NOT_ALLOWED_ON_THIS_ENDPOINT`

### State machine

- [ ] Every status change goes through transition service + policy **before** persist
- [ ] Illegal transition: **400**, row unchanged
- [ ] No second copy of allowed edges outside policy tests + policy class

### Ticket model

- [ ] JSON **excludes** internal numeric id
- [ ] `ticketId` server-generated on create

### RAG — retrieval policy

- [ ] **No literals `5` or `0.72`** inside `RagRetrievalService` for top-K / threshold defaults
- [ ] `@ConfigurationProperties` `rag.retrieval` constructor-injected
- [ ] Order: **top-K then threshold**

### RAG — ingestion

- [ ] No public reindex endpoint
- [ ] Ingest after commit; delete-by-`ticketId` then insert on success
- [ ] PGVector `ticket_chunk`, `<=>`, dim 768 (production path)

### Tests

- [ ] Default tests: H2, in-memory vectors, no Ollama in surefire ([rules/testing.md](../rules/testing.md))

---

## Search helpers (manual)

Run ripgrep-style checks (human or agent):

| Pattern | Expectation |
|---------|-------------|
| `INVALID_STATUS_TRANSITION` | Handler + tests |
| `STATUS_CHANGE_NOT_ALLOWED` | General PATCH handler |
| `0.72` in `RagRetrievalService` | **Absent** (ok in yaml/test config) |
| `localhost:11434` in `src/test` default | **Absent** or gated profile only |
| `@RestController` + `OPEN.*IN_PROGRESS` | **Absent** (not in controller) |

---

## Output format

```markdown
## Code review — <date> — <branch or commit>

### Violations (block merge)
1. file:line — rule/spec — description

### Gaps (incomplete implementation)
1. ...

### Compliant
- ...

### Checklists completed
- [x] java-springboot.md
- [x] api-standards.md
- [x] rag-vector-store.md
- [x] testing.md
```

Point to **spec section** or **rule file** for each violation.

---

## Rules of engagement

- Prefer **smallest** fix that restores compliance; do not expand scope in this review pass.
- If code and spec conflict, **spec wins** unless stakeholder updates spec first.