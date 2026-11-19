# Command: Review AI / RAG Implementation

**Purpose:** Targeted review of retrieval, generation, grounding gate, and RAG API mapping.  
**Specs:** [rag-ingestion.md](../spec/rag-ingestion.md), [rag-api-contract.md](../spec/rag-api-contract.md), [architecture.md](../spec/architecture.md)

---

## Scope

Focus on packages: `com.example.rag.*` and RAG-related calls from `com.example.ticket.service`.

---

## Checklist

### Retrieval & keep-set

- [ ] **Empty keep-set** after top-K + threshold → **LLM not invoked** (verify with test spy or code path audit)
- [ ] Response: HTTP **200**, `outcome` **`NO_MATCH`**, `noMatch.code` **`NO_RELEVANT_TICKETS`**, `citations` **`[]`**
- [ ] Retrieval order: embed → **top-K** → **threshold** filter

### Generator prompt

- [ ] Prompt contains **section 8.1 verbatim** from [rag-api-contract.md](../spec/rag-api-contract.md) (exact five sentences — diff against spec)
- [ ] Context passed to model is **only** keep-set chunk text (no full DB dump)

### Citations

- [ ] `citations[].ticketId` ⊆ keep-set ticket IDs
- [ ] **One citation per distinct** `ticketId` in keep-set for `ANSWER`
- [ ] Sort: **descending** best similarity per ticket, then **ascending** `ticketId`
- [ ] `title` loaded from **ticket repository**, not from model

### Confidence

- [ ] `confidence.score` = max similarity **s** over keep-set on `ANSWER`
- [ ] `confidence.basis` = **`RETRIEVAL`**

### Post-generation gate

- [ ] Ticket ID in answer text matching `TKT-[0-9]+` **not** in keep-set → **downgrade** to `NO_MATCH` / `GROUNDING_GATE_FAILED`, `answer` **null**
- [ ] **Duration contradiction** with context (e.g. 30 minutes in chunk, 24 hours in answer) → same downgrade
- [ ] Rejected draft **never** returned as `outcome: ANSWER`

### Infrastructure errors

- [ ] Embedding / vector / generation failures → `outcome: **ERROR**`, not `NO_MATCH`
- [ ] Codes: `EMBEDDING_FAILED`, `VECTOR_STORE_FAILED`, `GENERATION_FAILED`

### Envelope shape

- [ ] All keys present: `outcome`, `query`, `answer`, `citations`, `confidence`, `escalation`, `error`, `noMatch`

---

## Golden / eval cross-check

- [ ] `src/test/resources/eval/hallucination-duration-expiry.json` scenario passes in automated test
- [ ] [evaluation-strategy.md](../spec/evaluation-strategy.md) §8 satisfied

---

## Output format

```markdown
## RAG implementation review — <date>

### Failures (must fix)
1. ...

### Verified
- [x] Empty keep-set skips LLM
- [x] §8.1 prompt verbatim
- ...

### Evidence
- Test: `...`
- File: `...`
```

---

## Optional deep pass

Run [review-rag-output.md](review-rag-output.md) on sample production or staging answers with logged keep-set chunks.