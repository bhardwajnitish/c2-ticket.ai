# c2-ticket.ai — Evaluation Strategy

**Sources:** [requirements.md](requirements.md), [rag-ingestion.md](rag-ingestion.md), [rag-api-contract.md](rag-api-contract.md), [api-contract.md](api-contract.md)  
**Golden data:** `src/test/resources/eval/`  
**Document type:** Quality evaluation for retrieval, grounding, hallucination, and API contracts

---

## 1. Purpose

Define **how** c2-ticket.ai is evaluated beyond unit/integration tests: measurable checks for retrieval, grounding, hallucination resistance, and **stable JSON contracts**. Golden cases provide repeatable inputs and expected outcomes.

**Not in scope:** Production monitoring dashboards, human rubric scoring at scale, or auth.

---

## 2. Evaluation dimensions

| Dimension | Question | Pass criteria (summary) |
|-----------|----------|-------------------------|
| **Retrieval** | Did search return the right tickets and exclude junk? | Expected `ticketId`s appear in keep-set; irrelevant chunks below **similarity-threshold** absent |
| **Grounding** | Are answer claims supported by retrieved context? | Factual claims trace to keep-set chunk text |
| **Hallucination** | Does the system refuse unsafe model output? | Known bad answer **must not** surface as `outcome: ANSWER` |
| **API contract** | Do responses match documented shapes? | Ticket error envelope and RAG full envelope keys/types |

---

## 3. Retrieval evaluation

### 3.1 Method

| Step | Action |
|------|--------|
| 1 | Seed tickets + ingest (test profile: deterministic embeddings or optional real Ollama) |
| 2 | Run `RagRetrievalService` with fixed query text |
| 3 | Apply **top-K first**, then **threshold** ([rag-ingestion.md](rag-ingestion.md) §10) |

### 3.2 Assertions

| Check | Pass |
|-------|------|
| **Expected IDs present** | Every `expectedTicketIds[]` in golden case appears in keep-set |
| **Irrelevant below threshold** | Chunks with similarity **s < threshold** excluded; chunks tagged `belowThreshold: true` in fixture must not appear |
| **Config binding** | Overrides to `rag.retrieval.top-k` and `similarity-threshold` change results (cross-ref [test-strategy.md](test-strategy.md)) |

---

## 4. Grounding evaluation

### 4.1 Method

| Step | Action |
|------|--------|
| 1 | Fix keep-set chunk text (golden `contextChunks`) |
| 2 | Use generator stub returning `approvedAnswer` or `badAnswer` |
| 3 | Run post-generation gate per [rag-api-contract.md](rag-api-contract.md) §5.2 |

### 4.2 Assertions

| Check | Pass |
|-------|------|
| Supported claims | `approvedAnswer` only references facts present in `contextChunks` → gate pass → `ANSWER` |
| Citation set | Response `citations[].ticketId` ⊆ keep-set ticket IDs |

---

## 5. Hallucination evaluation

### 5.1 Controlled scenario (normative)

**Golden case:** `hallucination-duration-expiry.json` (see §7).

| Item | Value |
|------|--------|
| **Retrieved context** | States password reset links expire after **30 minutes** |
| **Bad model output** | Claims reset links expire after **24 hours** |
| **Gate** | Duration claim **contradicts** context → **FAIL** |
| **API outcome** | **`NO_MATCH`** — not `ANSWER` |
| **Expected `noMatch.code`** | **`GROUNDING_GATE_FAILED`** ([rag-api-contract.md](rag-api-contract.md) §5.2) |
| **Answer exposure** | `answer` must be **`null`** in response; rejected draft not returned |

This proves the pipeline **does not** return a guessed `ANSWER` when the model hallucinates a duration.

### 5.2 General rule

Any golden case with `"expectOutcome": "NO_MATCH"` and `"generatorOutput"` labeled bad must assert:

- HTTP **200**
- `outcome === "NO_MATCH"`
- `answer === null`

---

## 6. API contract evaluation

### 6.1 Ticket API ([api-contract.md](api-contract.md))

| Response type | Required shape |
|---------------|----------------|
| Error | `{ "error": { "code", "message", "details": [] } }` |
| Ticket success | `ticketId`, `title`, `description`, `priority`, `status`, `assignee`, `category`, `resolutionNotes`, `createdAt`, `updatedAt` — **no** internal numeric `id` |
| Comment success | `id` (UUID string), `ticketId`, `author`, `content`, `createdAt` |

Automated tests: assert keys on sample responses for create, list, 404, `VALIDATION_ERROR`, `INVALID_STATUS_TRANSITION`.

### 6.2 RAG API ([rag-api-contract.md](rag-api-contract.md))

| Rule | Test |
|------|------|
| All top-level keys always present | `outcome`, `query`, `answer`, `citations`, `confidence`, `escalation`, `error`, `noMatch` |
| `NO_MATCH` empty keep-set | `noMatch.code === "NO_RELEVANT_TICKETS"`, `citations === []`, LLM not invoked |
| `ERROR` | `escalation === null`, `error.code` in allowed set |

Contract checks run against `grounding-supported-answer.json` and `hallucination-duration-expiry.json` envelope fields; empty keep-set `NO_RELEVANT_TICKETS` is covered in MockMvc tests per [test-strategy.md](test-strategy.md).

---

## 7. Golden dataset

**Location:** `src/test/resources/eval/`

**Manifest:** `manifest.json` lists case ids and files.

| File | Intent |
|------|--------|
| `retrieval-expected-tickets.json` | Retrieval: expected ticket IDs in keep-set |
| `retrieval-below-threshold.json` | Irrelevant chunk excluded by threshold |
| `grounding-supported-answer.json` | Approved answer passes gate → `ANSWER` |
| `hallucination-duration-expiry.json` | **Controlled hallucination** — 30 min vs 24 h → `NO_MATCH` |

**Runner (implementation):** Load manifest in grounding/RAG integration tests; map `contextChunks` to keep-set; stub generator with `generatorOutput`; assert `expectOutcome` and `expectNoMatchCode` / `expectCitationTicketIds`.

### 7.1 Case schema (logical)

```json
{
  "id": "string",
  "description": "string",
  "query": "string",
  "expectedTicketIds": ["TKT-1001"],
  "contextChunks": [{ "ticketId": "TKT-1001", "content": "..." }],
  "belowThresholdChunks": [],
  "generatorOutput": "string or null",
  "expectOutcome": "ANSWER | NO_MATCH | ERROR",
  "expectNoMatchCode": "string or null",
  "expectCitationTicketIds": ["TKT-1001"],
  "contractChecks": ["rag-envelope-keys"]
}
```

Not every field required in every file — see per-case JSON.

---

## 8. Controlled hallucination (full specification)

This text is the **normative evaluation scenario** referenced in prompts and golden data.

1. **Setup:** Keep-set contains one or more chunks whose text includes: *password reset links expire after 30 minutes* (exact wording may vary in chunk but **30 minutes** is the authoritative duration in context).

2. **Stimulus:** Deterministic generator stub returns answer text asserting expiry of **24 hours** (or equivalent contradicting duration).

3. **Gate:** Post-generation **duration claim** check compares answer to context → **contradiction detected**.

4. **Expected API response:**
   - `outcome`: **`NO_MATCH`**
   - `answer`: **`null`**
   - `noMatch.code`: **`GROUNDING_GATE_FAILED`**
   - `citations`: **`[]`**
   - `escalation.recommended`: **`true`**

5. **Failure (evaluation fail):** If `outcome` is **`ANSWER`** with the bad 24-hour text exposed to the client, the evaluation **fails**.

---

## 9. Running evaluations

| Mode | Command pattern |
|------|-----------------|
| Default CI | Unit/integration tests load `src/test/resources/eval/*` with stubs |
| Local optional | Integration profile + real Ollama for retrieval-only eval (optional); hallucination case still uses stub generator |

Report: test name + golden `id` on failure for traceability.

---

## Document control

| Field | Value |
|-------|--------|
| Version | 1.0 |
| Golden data path | `src/test/resources/eval/` |