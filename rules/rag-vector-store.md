# RAG & Vector Store — Implementation Rules

**Type:** Checklist  
**Specs:** [spec/rag-ingestion.md](../spec/rag-ingestion.md), [spec/rag-api-contract.md](../spec/rag-api-contract.md), [spec/architecture.md](../spec/architecture.md)

---

## Vector store (PostgreSQL + PGVector)

- [ ] **Same PostgreSQL database** as tickets/comments (not a separate vector-only DB in default design)
- [ ] Table **`ticket_chunk`** with **`embedding vector(768)`** ([rag-ingestion.md](../spec/rag-ingestion.md))
- [ ] Similarity search uses cosine distance operator **`<=>`**
- [ ] Similarity score **`s = 1 - distance`** when comparing to threshold
- [ ] **HNSW** index on `embedding` with **`vector_cosine_ops`**
- [ ] Metadata columns match locked ingest snapshot: `ticket_id`, `chunk_index`, `content`, status/priority/assignee/category snapshots, fingerprint, models, `ingested_at`

---

## Configuration (`@ConfigurationProperties`)

- [ ] Prefix **`rag.retrieval`** bound to a properties type; **constructor-inject** into **`RagRetrievalService`**
- [ ] Defaults: **`top-k` = 5**, **`similarity-threshold` = 0.72** in `application.yml` only — **not in Java literals**
- [ ] **Forbidden:** literals `5` or `0.72` (or `0.72f`) inside **`RagRetrievalService`** body for policy defaults ([rag-ingestion.md](../spec/rag-ingestion.md) ING-D-10)
- [ ] Also use properties for `rag.chunk.*`, `rag.embedding.*` per ingestion spec

---

## Retrieval order (mandatory)

In **`RagRetrievalService`** (or equivalent):

1. [ ] Embed query (same model as ingest in dev: **nomic-embed-text**)
2. [ ] Vector search with **LIMIT top-k** (from config)
3. [ ] Filter results where **`s >= similarity-threshold`**
4. [ ] Emit **keep-set** (chunks + ticket IDs)

**Not allowed:** threshold before top-k, or hardcoded K/threshold.

---

## Ingestion

- [ ] **Internal only** — no client sync API
- [ ] Run **synchronously after** ticket/comment/status transaction **commits**
- [ ] Failed ingest **does not roll back** ticket data; on embed failure **log** and **leave previous vectors**
- [ ] **Replace semantics:** within one transaction — **`DELETE` all `ticket_chunk` for `ticketId`**, then **`INSERT`** new rows — only after successful embed (or fingerprint skip per spec)
- [ ] **No usable content** → delete all chunks for ticket, insert zero
- [ ] Re-ingest on: **create**, **field update**, **comment add**, **status change**
- [ ] Chunker version **`section-v1`**; prefix `[Ticket {ticketId}] {title}\n` on every chunk

---

## Query / generation pipeline

- [ ] **Empty keep-set** → **no LLM call**; API **`NO_MATCH`** / `NO_RELEVANT_TICKETS` ([rag-api-contract.md](../spec/rag-api-contract.md))
- [ ] Generator receives **only** keep-set chunk text
- [ ] Prompt includes **§8.1 verbatim** grounding instructions ([rag-api-contract.md](../spec/rag-api-contract.md) §8.1)
- [ ] **Citations** assembled from keep-set ticket IDs + titles from ticket store — **not** parsed from model text
- [ ] Post-generation **gate**: foreign ticket IDs in answer or **duration contradiction** → downgrade to **`NO_MATCH`** / `GROUNDING_GATE_FAILED`; **`answer` null**

---

## Dev vs production embeddings

- [ ] Dev path: Ollama **`http://localhost:11434`**, **`nomic-embed-text`**, dimension **768**
- [ ] Production cloud embeddings optional; must still honor config and dimension **768** for PGVector column

---

## Sign-off

- [ ] [commands/review-ai.md](../commands/review-ai.md)  
- [ ] [commands/review-code.md](../commands/review-code.md) RAG sections  
- [ ] Optional integration profile only for live Ollama + PGVector ([rules/testing.md](testing.md))