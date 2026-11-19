# c2-ticket.ai

AI-assisted support ticket management: REST ticket API, post-commit RAG ingestion into PGVector, and a grounded **Ask** flow (`POST /api/v1/rag/query`). Specifications live in [`spec/`](spec/); implementation rules in [`rules/`](rules/).

## Stack

| Layer | Technology |
|--------|------------|
| Backend | Java 17, Spring Boot 3.5.6, Spring AI 1.1.5, Spring Data JPA, Flyway |
| Database | PostgreSQL + **PGVector** (`ticket_chunk`, migration **V5**) |
| Embeddings (dev) | Ollama **nomic-embed-text** (768-d), `http://localhost:11434` |
| Generation (dev) | Ollama **llama3.2** |
| Frontend | Next.js 15, React 19, TypeScript (no UI library) |
| Default tests | H2 in-memory, deterministic test embeddings, in-memory vector store |

## Repository layout

```
c2-ticket.ai/
├── backend/                 # Spring Boot app (artifact: c2-ticket-ai)
│   ├── src/main/java/com/example/
│   │   ├── ticket/          # domain, api, service, repository
│   │   ├── rag/             # ingestion, retrieval, generation, grounding, query API
│   │   └── config/          # web prefix, CORS, RAG @ConfigurationProperties
│   └── src/main/resources/
│       ├── application*.yaml
│       └── db/migration/    # V1–V5 (V5 = PGVector ticket_chunk)
├── frontend/                # Next.js app (c2-ticket-ai-frontend)
├── spec/                    # requirements, API contracts, RAG, UI flow
├── rules/                   # implementation checklists
├── commands/                # review commands (code, RAG output, etc.)
├── docs/                    # implementation plan, prompt history
└── src/test/resources/eval/ # golden eval JSON (repo root; mirror in backend tests)
```

## Prerequisites (local dev with real RAG)

1. **PostgreSQL** database:

   | Setting | Value |
   |---------|--------|
   | Database | `c2_ticket_ai` |
   | User | `c2_ticket_ai` |
   | Password | `c2_ticket_ai` |

   Create the database and user, then start Postgres.

2. **PGVector**: applied by Flyway **`V5__pgvector_ticket_chunk.sql`** on startup (`CREATE EXTENSION vector`, `ticket_chunk`, HNSW `vector_cosine_ops`).

3. **Ollama** on **`http://localhost:11434`** with models:

   - `nomic-embed-text` (embeddings)
   - `llama3.2` (chat / RAG answers in dev)

## Run the system

### Backend (port **8080**)

```bash
cd backend
mvn spring-boot:run
```

Uses profile **`dev`** (`application-dev.yaml`): JDBC to `c2_ticket_ai`, Flyway on, Ollama for embed + chat.

API base path: **`/api/v1`**.

### Frontend (port **3000**)

```bash
cd frontend
cp .env.local.example .env.local   # optional; default API http://localhost:8080/api/v1
npm install
npm run dev
```

Routes: `/tickets`, `/tickets/new`, `/tickets/[ticketId]`, `/ask` (home redirects to `/tickets`).

### Tests (no Postgres or Ollama required)

```bash
cd backend && mvn test    # H2 + test profile; in-memory vectors (73 tests at last green run)
cd frontend && npm test   # Vitest (18 tests at last green run)
```

Optional Ollama calibration (not in default CI): `RUN_OLLAMA_CALIBRATION=true` with profile `ollama-calibration`.

## API examples (`curl`)

Base URL: `http://localhost:8080/api/v1`

### Create ticket

```bash
curl -s -X POST http://localhost:8080/api/v1/tickets \
  -H 'Content-Type: application/json' \
  -d '{"title":"Login failure","description":"Cannot sign in","priority":"HIGH","assignee":"alice","category":"auth"}'
```

### List tickets (AND filters)

```bash
curl -s 'http://localhost:8080/api/v1/tickets?status=OPEN&priority=HIGH'
```

### Get ticket

```bash
curl -s http://localhost:8080/api/v1/tickets/TKT-1001
```

### Patch fields (not status)

```bash
curl -s -X PATCH http://localhost:8080/api/v1/tickets/TKT-1001 \
  -H 'Content-Type: application/json' \
  -d '{"description":"Updated steps","resolutionNotes":"Reset cache"}'
```

### Status transition (only on status endpoint)

```bash
curl -s -X PATCH http://localhost:8080/api/v1/tickets/TKT-1001/status \
  -H 'Content-Type: application/json' \
  -d '{"status":"IN_PROGRESS"}'
```

### Comments

```bash
curl -s -X POST http://localhost:8080/api/v1/tickets/TKT-1001/comments \
  -H 'Content-Type: application/json' \
  -d '{"author":"bob","content":"Reproduced on Chrome"}'

curl -s http://localhost:8080/api/v1/tickets/TKT-1001/comments
```

### RAG query

```bash
curl -s -X POST http://localhost:8080/api/v1/rag/query \
  -H 'Content-Type: application/json' \
  -d '{"query":"How was TKT-1001 resolved?"}'
```

Response always includes all envelope keys (`outcome`, `query`, `answer`, `citations`, `confidence`, `escalation`, `error`, `noMatch`). See [`spec/rag-api-contract.md`](spec/rag-api-contract.md).

## Ticket state machine

Enforced in **`TicketStatusTransitionPolicy`**; changes only via `PATCH .../status`. Same-state and terminal states reject transitions (`400` / `INVALID_STATUS_TRANSITION`).

| From | Allowed to |
|------|------------|
| `OPEN` | `IN_PROGRESS`, `CANCELLED` |
| `IN_PROGRESS` | `RESOLVED`, `CANCELLED` |
| `RESOLVED` | `CLOSED` |
| `CLOSED` | *(none)* |
| `CANCELLED` | *(none)* |

## RAG pipeline (summary)

### Ingestion (post-commit)

After create/update/comment/status on a ticket: build knowledge document → **section-v1** chunking → embed → **replace** all `ticket_chunk` rows for that `ticketId` (delete then insert). Empty document deletes chunks. Embedding failure is logged; ticket commit stands; previous vectors remain.

Chunk prefix: `[Ticket {ticketId}] {title}\n`. Chunking uses **`rag.chunk.max-characters`** / **`overlap-characters`** (defaults 2000 / 250). Config keys **`max-tokens`** / **`overlap-tokens`** exist but the chunker is **character-based**.

### Retrieval

`RagRetrievalService`:

1. Embed trimmed query  
2. Vector search (cosine **distance** `<=>`)  
3. **Top-K** (default **5**), then **similarity threshold** (default **0.72**)  
4. Similarity **s = 1 − distance**  
5. Empty keep-set → **`NO_MATCH`** / `NO_RELEVANT_TICKETS`, **no LLM**

Policy values come from `rag.retrieval.*`, not hardcoded in code.

### Generation and grounding

Non-empty keep-set → Ollama **llama3.2** with §8.1 grounding instructions → **post-generation gate**:

- Any `TKT-[0-9]+` in the answer must be in the keep-set  
- Duration claims must not contradict context (e.g. 30 minutes in chunks vs 24 hours in answer)

Gate failure → **`NO_MATCH`** / `GROUNDING_GATE_FAILED` (rejected text not returned). Success → **`ANSWER`** with citations from keep-set only (titles from DB).

### NO_MATCH vs ERROR

| Situation | `outcome` | HTTP (typical) |
|-----------|-----------|----------------|
| Empty keep-set | `NO_MATCH` | 200 |
| Grounding gate failed | `NO_MATCH` | 200 |
| Embed / vector / LLM failure | `ERROR` | 503 |

## Known limits (v1)

- **No authentication**, **no pagination**, **no delete** for tickets or comments  
- List API: filters only (`status`, `priority`, `assignee`, `category`) — **no server keyword search** (UI filters keywords client-side)  
- **PGVector SQL path** is not exercised in default `mvn test` (in-memory vector store in test profile)  
- Grounding gate is **strong on ticket IDs and duration claims**; other hallucinations may need broader checks  
- Chunking is **character-based**; token-related chunk config keys are unused by the chunker  

## Further reading

- [`spec/requirements.md`](spec/requirements.md) — product rules  
- [`spec/api-contract.md`](spec/api-contract.md) — REST tickets & comments  
- [`spec/rag-api-contract.md`](spec/rag-api-contract.md) — Ask API envelope  
- [`spec/rag-ingestion.md`](spec/rag-ingestion.md) — ingestion & retrieval config  
- [`docs/prompt-history.md`](docs/prompt-history.md) — build prompt chronology  
- [`docs/implementation-plan.md`](docs/implementation-plan.md) — phased plan  