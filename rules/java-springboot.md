# Java & Spring Boot — Implementation Rules

**Type:** Checklist for implementers (not application code)  
**Specs:** [spec/architecture.md](../spec/architecture.md), [spec/api-contract.md](../spec/api-contract.md)

---

## Platform

- [ ] **Java 17** (language level and toolchain)
- [ ] **Spring Boot 3.5.x** (parent BOM / dependency management)
- [ ] **Constructor injection** only for required dependencies (no field `@Autowired` on services)
- [ ] **Records** for DTOs / API request and response types (immutable transport)
- [ ] **Controllers contain no business rules** — validate HTTP shape, delegate to services; no transition matrix, no RAG policy, no retrieval threshold logic in controllers

---

## Package layout (mandatory roots)

### Tickets

| Package | Responsibility |
|---------|----------------|
| `com.example.ticket.api` | REST controllers, request/response records, exception handlers mapping to API error envelope |
| `com.example.ticket.domain` | `TicketStatusTransitionPolicy`, enums, domain types with no Spring/Web dependencies |
| `com.example.ticket.service` | Application services (create, update, transition, comments orchestration) |
| `com.example.ticket.repository` | JPA entities (if used), Spring Data repositories — persistence only |

### RAG

| Package | Responsibility |
|---------|----------------|
| `com.example.rag.api` | `POST /api/v1/rag/query` controller only |
| `com.example.rag.config` | `@ConfigurationProperties` for `rag.chunk`, `rag.embedding`, `rag.retrieval`, `rag.ingestion` |
| `com.example.rag.ingestion` | Post-commit ingest, chunker `section-v1`, fingerprint, replace semantics |
| `com.example.rag.retrieval` | Embed query, top-K, threshold, keep-set assembly — **`RagRetrievalService`** lives here or under `service` per team choice; must not hardcode K/threshold |
| `com.example.rag.generation` | LLM port, prompt assembly including §8.1 verbatim text from [rag-api-contract.md](../spec/rag-api-contract.md) |
| `com.example.rag.review` | Post-generation gate (ticket IDs, duration contradictions) |
| `com.example.rag.service` | RAG orchestration (retrieval → gate → optional generation → citations) |

### Shared

| Package | Responsibility |
|---------|----------------|
| `com.example.config` | Spring configuration, profiles (`test`, optional `it-ollama-pg`), beans shared across modules |

---

## Layering rules

- [ ] **Domain** (`ticket.domain`) has **zero** imports from `api`, `service`, `repository`, `rag.*`
- [ ] **State machine** implemented only in `TicketStatusTransitionPolicy` ([state-machine.md](../spec/state-machine.md))
- [ ] **Ticket status** mutated only through transition service → policy → repository
- [ ] **RAG pipeline** orchestrated in `rag.service`; ingestion invoked from ticket service **after commit** ([rag-ingestion.md](../spec/rag-ingestion.md))
- [ ] **Citation assembly** not in `generation` — citations built from keep-set in orchestration/review layer ([architecture.md](../spec/architecture.md) T6)

---

## Spring conventions

- [ ] Use `@ConfigurationProperties` for `rag.*` and document in `application.yml` — no magic literals for retrieval ([rules/rag-vector-store.md](rag-vector-store.md))
- [ ] `@Transactional` on application services where appropriate; ingest **after** ticket transaction commits
- [ ] Global exception handler in `ticket.api` (and RAG handler if separate) maps domain failures to stable `error.code` values
- [ ] Do not expose internal surrogate `id` in JSON ([data-model.md](../spec/data-model.md))

---

## Forbidden patterns

- [ ] Business rules in `@RestController` classes
- [ ] Duplicate status transition matrix outside `TicketStatusTransitionPolicy`
- [ ] Parsing model output to build `citations` array
- [ ] Client-callable reindex / sync endpoints

---

## Sign-off

Before merge, run [commands/review-code.md](../commands/review-code.md) and confirm each box above for touched packages.