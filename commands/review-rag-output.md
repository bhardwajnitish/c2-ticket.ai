# Command: Review RAG Output (manual / semi-automated)

**Purpose:** Given **one** candidate answer and its **retrieved chunk texts**, decide **PASS** or **FAIL** and list **every unsupported claim**.  
**Does not** modify source files.

---

## Inputs (required)

| Input | Description |
|-------|-------------|
| `answerText` | Final model answer text **or** `answer.text` the API would return on `ANSWER` (for gate review, include draft before downgrade) |
| `keepSetTicketIds` | List of ticket IDs that were in the retrieval keep-set |
| `contextChunks` | Array of `{ "ticketId": "TKT-…", "content": "…" }` — exact text supplied to the generator |
| `citations` (optional) | API `citations` array if already assembled |

---

## Procedure

### Step 1 — Invented ticket IDs

- [ ] Extract all substrings matching **`TKT-[0-9]+`** from `answerText`
- [ ] **FAIL** if any ID **not** in `keepSetTicketIds`
- [ ] Record each offending ID as unsupported claim: *"Invented or out-of-keep-set ticket ID: TKT-…"*

### Step 2 — Citation consistency (if `citations` provided)

- [ ] Every `citations[].ticketId` ∈ `keepSetTicketIds`
- [ ] **FAIL** if citations include an ID not in keep-set
- [ ] **FAIL** if `citations` include ID not mentioned in answer when answer makes factual claims about specific tickets (optional strict mode)

### Step 3 — Factual claims vs context

Decompose `answerText` into **factual claims** (sentences or clauses about tickets, events, causes, resolutions, users, **dates**, **durations**, status).

For **each** claim:

| Check | PASS condition |
|-------|----------------|
| Support | Claim is entailed by or explicitly stated in **some** `contextChunks[].content` |
| No general knowledge | Support-specific facts are not justified only by world knowledge |
| Dates | Any date/month/year in answer appears in context or is clearly paraphrase of same instant |
| Durations | Numeric time claims (minutes, hours, days) match context or conservative paraphrase |

**FAIL** each unsupported claim with one line:

```text
UNSUPPORTED: "<claim excerpt>" — not found in context for TKT-… (or: contradicts context)
```

### Step 4 — Duration contradictions (required check)

- [ ] If context states a duration (e.g. **30 minutes**), answer stating **24 hours** (or any incompatible duration for same subject) → **FAIL**
- [ ] Map to product rule: gate must yield **`NO_MATCH`**, not `ANSWER` ([evaluation-strategy.md](../spec/evaluation-strategy.md) §8)

### Step 5 — Dates and events

- [ ] **FAIL** on invented dates/events/users not present in any chunk
- [ ] **FAIL** on resolutions/causes attributed to a ticket with no support in that ticket’s chunk text

---

## Verdict

| Result | When |
|--------|------|
| **PASS** | Zero unsupported claims; all ticket IDs ⊆ keep-set; no contradictions |
| **FAIL** | One or more unsupported claims or rule violations |

---

## Output format (required)

```markdown
## RAG output review

**Verdict:** PASS | FAIL

### keep-set ticket IDs
TKT-1001, ...

### Unsupported claims
1. ...
(or "None")

### Ticket ID violations
- ...

### Duration / date violations
- ...

### Notes
- ...
```

---

## Example (controlled hallucination)

| Field | Value |
|-------|--------|
| Context | "... password reset links expire after **30 minutes**." |
| Answer | "Reset links expire after **24 hours**." |
| **Verdict** | **FAIL** — duration contradicts context; must not ship as `ANSWER` |

---

## Rules of engagement

- Judge **only** against supplied `contextChunks`, not live DB or general knowledge.
- When used in CI, implement as a test that feeds fixture answer + chunks and asserts **FAIL** for golden bad answers.
- This command **does not** change application code; file findings in PR comments or test failures.