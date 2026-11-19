# Command: Review Specs

**Purpose:** Cross-check specification documents for internal consistency.  
**Constraint:** **Do not change any files** during this review — report only.

---

## Inputs

Read all files under `spec/` (at minimum):

| Document |
|----------|
| [requirements.md](../spec/requirements.md) |
| [architecture.md](../spec/architecture.md) |
| [data-model.md](../spec/data-model.md) |
| [state-machine.md](../spec/state-machine.md) |
| [rag-ingestion.md](../spec/rag-ingestion.md) |
| [rag-api-contract.md](../spec/rag-api-contract.md) |
| [api-contract.md](../spec/api-contract.md) |
| [ui-flow.md](../spec/ui-flow.md) |
| [test-strategy.md](../spec/test-strategy.md) |
| [evaluation-strategy.md](../spec/evaluation-strategy.md) |

Optional: `rules/*`, `docs/prompt-history.md` for traceability only.

---

## Procedure (checklist)

### 1. State machine

- [ ] Allowed transitions identical across `requirements.md`, `state-machine.md`, `architecture.md`, `ui-flow.md`
- [ ] Terminal states `CLOSED`, `CANCELLED` consistent
- [ ] Status change only on dedicated PATCH path in `api-contract.md` + `state-machine.md`
- [ ] Error code `INVALID_STATUS_TRANSITION` aligned

### 2. Data model vs API

- [ ] Ticket fields in `api-contract.md` match `data-model.md` (types, max lengths, enums)
- [ ] `ticketId` generation rules consistent; never client-supplied on create
- [ ] Comment UUID and FK on `ticketId` consistent

### 3. RAG ingestion vs architecture

- [ ] Post-commit ingest, no client sync — `architecture.md`, `rag-ingestion.md`, `rules/rag-vector-store.md`
- [ ] top-K **then** threshold — `rag-ingestion.md`, `architecture.md`, `rag-api-contract.md`
- [ ] Empty keep-set, no LLM — `requirements.md`, `rag-api-contract.md`
- [ ] Citations from keep-set, not model parsing — `architecture.md`, `rag-api-contract.md`

### 4. RAG API vs evaluation

- [ ] `NO_MATCH` codes: `NO_RELEVANT_TICKETS`, `GROUNDING_GATE_FAILED`
- [ ] Controlled hallucination (30 min vs 24 h) in `evaluation-strategy.md` matches gate rules in `rag-api-contract.md` §5.2

### 5. UI vs API

- [ ] List filters match `GET /tickets` params only
- [ ] Keyword search client-only — no API keyword param
- [ ] Status buttons match transition matrix

### 6. Test / eval vs product specs

- [ ] H2/fakes default vs Ollama optional — `test-strategy.md`, `rules/testing.md`
- [ ] Golden eval files referenced in `evaluation-strategy.md` exist under `src/test/resources/eval/`

### 7. Open questions

- [ ] List items marked **open** in one spec that are **locked** in another → **contradiction**

---

## Output format (required)

```markdown
## Spec review — <date>

### Contradictions (must fix in specs before implementation)
1. ...

### Warnings (ambiguity, not hard conflict)
1. ...

### Aligned areas
- ...

### Checked documents
- [x] requirements.md
- ...
```

If **no contradictions**: state explicitly **"No contradictions found."**

---

## Rules of engagement

- **Do not** edit spec files while executing this command.
- **Do not** invent new product requirements; only compare existing text.
- Escalate contradictions to stakeholders; implementation should not guess.