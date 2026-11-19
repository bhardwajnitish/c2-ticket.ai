# c2-ticket.ai — Requirements Analysis

**Product:** AI-powered support ticket management system  
**Document type:** Structured requirements analysis (architecture / BA)  
**Scope of this document:** Requirements only — no application design or implementation.

**Classification legend**

| Tag | Meaning |
|-----|---------|
| **Specified** | Explicitly stated in project brief or mandatory rules |
| **Assumption** | Reasonable inference for a ticket + RAG system; not stated — must be confirmed |
| **Open question** | Material gap; stakeholders must decide |

---

## 1. Functional requirements

| ID | Requirement | Classification |
|----|-------------|----------------|
| FR-1 | The system manages **support tickets** as the core work item. | **Assumption** (implied by product name and “ticket management”) |
| FR-2 | Tickets follow a defined **status lifecycle** with allowed transitions enforced by the **backend** (not UI-only). | **Specified** |
| FR-3 | **Terminal statuses:** `CLOSED` and `CANCELLED` — no further status transitions after reaching either. | **Specified** |
| FR-4 | **Allowed forward path:** `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`. | **Specified** |
| FR-5 | **Cancellation path:** `OPEN` → `CANCELLED` and `IN_PROGRESS` → `CANCELLED`. | **Specified** |
| FR-6 | **Same-status transitions** (e.g. `OPEN` → `OPEN`) are **rejected**. | **Specified** |
| FR-7 | **Any transition not listed** as allowed is **rejected by the backend**. | **Specified** |
| FR-8 | Users can **query** the system using natural language; responses are produced via a **RAG** pipeline over ticket data. | **Assumption** (implied by “AI-powered” + RAG rules) |
| FR-9 | When retrieval finds **no relevant tickets** (per policy), the system returns an **explicit no-match response** and **does not invoke the LLM**. | **Specified** |
| FR-10 | When the LLM is invoked, answers must use **only** ticket content from the **retrieval keep-set** and **cite ticket IDs** from that set. | **Specified** |
| FR-11 | **Ticket data changes** trigger **refresh of embeddings** used for retrieval. | **Specified** |
| FR-12 | Retrieval uses **configurable** `top-K` and **similarity threshold** (not hardcoded). | **Specified** |
| FR-13 | Ability to **create, read, update** ticket records (minimum for management + embedding source). | **Assumption** |
| FR-14 | Ability to **transition ticket status** via an API or command that validates the state machine. | **Assumption** (required to realize FR-2–FR-7) |
| FR-15 | **Authentication / authorization** for who may change tickets vs. ask RAG questions. | **Open question** |
| FR-16 | **Multi-tenant** isolation (org/workspace scoping for tickets and retrieval). | **Open question** |
| FR-17 | **Comments, attachments, assignees, priorities, SLAs, notifications.** | **Open question** |
| FR-18 | **Admin UI** vs. API-only clients. | **Open question** |

---

## 2. Non-functional requirements

| ID | Requirement | Classification |
|----|-------------|----------------|
| NFR-1 | **Backend enforcement** of ticket state transitions (defense in depth vs. UI). | **Specified** |
| NFR-2 | **Deterministic retrieval gate:** no LLM call on empty/no-match retrieval outcome. | **Specified** |
| NFR-3 | **Configurability:** `top-K` and similarity threshold adjustable without code change (e.g. configuration store/env). | **Specified** |
| NFR-4 | **Embedding freshness:** index reflects ticket updates within an acceptable delay after data change. | **Specified** (refresh required); **Open question** (max acceptable latency) |
| NFR-5 | **Auditability** of status transitions and AI answers (who/when/what). | **Open question** |
| NFR-6 | **Availability / uptime** targets. | **Open question** |
| NFR-7 | **Scalability** (ticket volume, embedding index size, query QPS). | **Open question** |
| NFR-8 | **Data retention** and deletion (tickets, embeddings, query logs). | **Open question** |
| NFR-9 | **PII / compliance** (GDPR, data residency). | **Open question** |
| NFR-10 | **Observability:** metrics for retrieval hit rate, no-match rate, LLM latency, embedding lag. | **Assumption** (operability of RAG + sync) |

---

## 3. Domain entities

Entities below are **minimum conceptual model** to satisfy specified rules. Fields and relationships not stated are marked.

### 3.1 Ticket

| Attribute / behavior | Notes | Classification |
|----------------------|-------|----------------|
| **Identifier** (`ticketId`) | Required for citations in RAG answers | **Specified** (citation rule) |
| **Status** | One of: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED` | **Specified** |
| **Content** searchable/embeddable | Source for RAG ingestion | **Assumption** |
| Title, description, metadata | — | **Open question** |
| Created/updated timestamps | Useful for embedding refresh triggers | **Assumption** |
| Creator, assignee, customer | — | **Open question** |

### 3.2 Ticket status transition (event)

| Attribute / behavior | Notes | Classification |
|----------------------|-------|----------------|
| From-status, to-status | Must satisfy state machine | **Specified** |
| Rejection on illegal transition | Backend validation | **Specified** |
| Actor, reason, timestamp | — | **Open question** |

### 3.3 Embedding / vector index record

| Attribute / behavior | Notes | Classification |
|----------------------|-------|----------------|
| Linked to ticket (or chunk) | Refreshed on ticket data change | **Specified** |
| Vector representation of ticket text | — | **Assumption** |
| Chunking strategy | Single ticket vs. multi-chunk | **Open question** |

### 3.4 RAG query

| Attribute / behavior | Notes | Classification |
|----------------------|-------|----------------|
| User query text | — | **Assumption** |
| Retrieval keep-set (ticket IDs + context) | Bounds LLM context and citations | **Specified** |
| Response: answer + citations OR no-match | — | **Specified** |

### 3.5 Configuration

| Attribute / behavior | Notes | Classification |
|----------------------|-------|----------------|
| `top-K` | Configurable | **Specified** |
| Similarity threshold | Configurable | **Specified** |
| LLM / embedding model selection | — | **Open question** |

---

## 4. API requirements

No REST shape was specified. Below are **capability-level** API requirements.

| ID | Capability | Behavior | Classification |
|----|------------|----------|----------------|
| API-1 | **Transition ticket status** | Accepts target status; validates allowed edges; rejects same-status and illegal transitions with error response | **Specified** (rules); **Assumption** (REST/resource shape) |
| API-2 | **Get ticket(s)** | Read ticket by ID and/or list with filters | **Assumption** |
| API-3 | **Create / update ticket** | Mutations that trigger embedding refresh | **Assumption** + **Specified** (embedding refresh) |
| API-4 | **RAG query** | Runs retrieval → if keep-set empty below threshold, return **no-match** without LLM; else call LLM with keep-set only; return answer with **ticket ID citations** | **Specified** |
| API-5 | **Read/update RAG config** | `top-K`, similarity threshold | **Specified** |
| API-6 | Standard error contract | Machine-readable codes for invalid transition, validation errors | **Assumption** |
| API-7 | Auth headers / OAuth / API keys | — | **Open question** |
| API-8 | Pagination, sorting, idempotency for transitions | — | **Open question** |
| API-9 | Webhooks on status change | — | **Open question** |

**Invalid transition (API-1)** — minimum behavior:

- Request rejected by server (not silent no-op).
- Same-status request rejected.
- Transitions from `CLOSED` or `CANCELLED` rejected.
- Disallowed edges (e.g. `OPEN` → `CLOSED`, `RESOLVED` → `OPEN`) rejected.

| Classification |
|----------------|
| **Specified** |

---

## 5. Ticket state-machine requirements

### 5.1 States

| State | Terminal? | Classification |
|-------|-----------|----------------|
| `OPEN` | No | **Specified** |
| `IN_PROGRESS` | No | **Specified** |
| `RESOLVED` | No | **Specified** |
| `CLOSED` | Yes | **Specified** |
| `CANCELLED` | Yes | **Specified** |

### 5.2 Allowed transitions

| From | To | Classification |
|------|-----|----------------|
| `OPEN` | `IN_PROGRESS` | **Specified** |
| `IN_PROGRESS` | `RESOLVED` | **Specified** |
| `RESOLVED` | `CLOSED` | **Specified** |
| `OPEN` | `CANCELLED` | **Specified** |
| `IN_PROGRESS` | `CANCELLED` | **Specified** |

### 5.3 Disallowed transitions (non-exhaustive examples)

| From | To | Reason | Classification |
|------|-----|--------|----------------|
| Any | Same state | Same-status rejected | **Specified** |
| `CLOSED` | Any | Terminal | **Specified** |
| `CANCELLED` | Any | Terminal | **Specified** |
| `OPEN` | `RESOLVED`, `CLOSED` | Not in allowed graph | **Assumption** (derived from allowed set only) |
| `RESOLVED` | `IN_PROGRESS`, `CANCELLED` | Not listed | **Assumption** (derived) |
| `IN_PROGRESS` | `OPEN` | Not listed | **Assumption** (derived) |

### 5.4 Enforcement

| Rule | Classification |
|------|----------------|
| Validation runs on **every** status change request in **backend** | **Specified** |
| UI may mirror rules for UX but is **not** authoritative | **Specified** |

### 5.5 Open questions (state machine)

| Question | Classification |
|----------|----------------|
| Can `RESOLVED` → `CANCELLED`? | **Open question** |
| Can `RESOLVED` → `IN_PROGRESS` (reopen)? | **Open question** |
| Are transitions **idempotent** if client retries? | **Open question** |

---

## 6. RAG ingestion requirements

| ID | Requirement | Classification |
|----|-------------|----------------|
| ING-1 | Ingest **ticket data** into a searchable/vector index for retrieval. | **Assumption** |
| ING-2 | On **ticket data change**, **re-embed** (refresh) affected index entries. | **Specified** |
| ING-3 | Define what “ticket data” includes (description only vs. comments, status, metadata). | **Open question** |
| ING-4 | **Incremental** update vs. full reindex on change. | **Open question** |
| ING-5 | Embedding model and dimensions. | **Open question** |
| ING-6 | Text normalization (HTML strip, language). | **Open question** |
| ING-7 | Deletion: remove vectors when ticket deleted. | **Open question** |
| ING-8 | Ordering/consistency: query during refresh (stale vs. blocking). | **Open question** |
| ING-9 | Store `ticketId` (and chunk id if chunked) on every index record for citation. | **Specified** (citation from keep-set) |

---

## 7. RAG retrieval requirements

| ID | Requirement | Classification |
|----|-------------|----------------|
| RET-1 | Retrieve top candidates using vector similarity (or equivalent) against the query. | **Assumption** |
| RET-2 | Apply **`top-K`** limit from configuration. | **Specified** |
| RET-3 | Apply **similarity threshold** from configuration; results below threshold excluded. | **Specified** |
| RET-4 | Produce a **keep-set**: ticket IDs + context payloads passed to generation (if any). | **Specified** |
| RET-5 | If keep-set is **empty** (no relevant tickets), return **explicit no-match** and **do not call LLM**. | **Specified** |
| RET-6 | Definition of “relevant” = post-threshold, post-top-K policy. | **Assumption** |
| RET-7 | Scope retrieval to tenant/user visibility rules. | **Open question** |
| RET-8 | Hybrid search (keyword + vector). | **Open question** |
| RET-9 | Re-ranking model. | **Open question** |

---

## 8. AI grounding and hallucination risks

### 8.1 Grounding requirements (specified)

| ID | Control | Classification |
|----|---------|----------------|
| GR-1 | LLM context **limited to retrieved keep-set** ticket content only. | **Specified** |
| GR-2 | Answers must **cite ticket IDs** present in the keep-set. | **Specified** |
| GR-3 | **No LLM invocation** when retrieval yields no qualifying tickets. | **Specified** |

### 8.2 Residual risks

| Risk | Description | Mitigation direction | Classification |
|------|-------------|---------------------|----------------|
| HR-1 | Model paraphrases or invents facts not in keep-set | Prompting + post-validation that claims map to cited tickets | **Assumption** (risk); **Open question** (mandatory validation) |
| HR-2 | Citations present but **wrong ticket** for claim | Citation-to-span alignment checks | **Open question** |
| HR-3 | **Stale** embeddings after update | ING-2 refresh; define max staleness | **Specified** + **Open question** (SLA) |
| HR-4 | Threshold too low → irrelevant context → hallucination | Tunable threshold; monitoring | **Specified** (config) + **Assumption** |
| HR-5 | Threshold too high → false no-match | Monitoring; tuning | **Assumption** |
| HR-6 | Prompt injection via ticket body | Sanitize/limit instructions in ticket text | **Open question** |
| HR-7 | Leakage across tenants if retrieval mis-scoped | AuthZ on index partitions | **Open question** |

---

## 9. Validation and error-handling requirements

| ID | Requirement | Classification |
|----|-------------|----------------|
| VAL-1 | Reject illegal status transitions with clear **client error** (4xx class assumed). | **Specified** (reject); **Assumption** (HTTP semantics) |
| VAL-2 | Reject same-status transition. | **Specified** |
| VAL-3 | Reject transitions from terminal states. | **Specified** |
| VAL-4 | Validate `top-K` and threshold (positive K, threshold in valid range). | **Assumption** |
| VAL-5 | Validate RAG query input (empty query, max length). | **Open question** |
| VAL-6 | Embedding refresh failures: retry, DLQ, surface alerts. | **Open question** |
| VAL-7 | LLM timeout/failure: user-visible error without partial uncited answer. | **Open question** |
| VAL-8 | Do not call LLM on retrieval failure/empty keep-set (treat as no-match or distinct error). | **Specified** for empty/no relevant; **Open question** for infra failure vs. no-match |

---

## 10. Testing requirements

| ID | Requirement | Classification |
|----|-------------|----------------|
| TST-1 | **State machine:** all **allowed** transitions succeed (from each non-terminal state). | **Specified** + **Assumption** (test obligation) |
| TST-2 | **State machine:** all **disallowed** transitions fail at **backend** (including same-status, terminal, unlisted edges). | **Specified** + **Assumption** |
| TST-3 | **RAG:** empty keep-set → no-match response, **LLM not called** (mock/spy). | **Specified** + **Assumption** |
| TST-4 | **RAG:** non-empty keep-set → LLM called with **only** keep-set context; response includes **citations** subset of keep-set IDs. | **Specified** + **Assumption** |
| TST-5 | **RAG:** changing config `top-K` / threshold changes retrieval behavior. | **Specified** + **Assumption** |
| TST-6 | **Ingestion:** ticket update triggers embedding refresh (integration test). | **Specified** + **Assumption** |
| TST-7 | Contract tests for public APIs. | **Assumption** |
| TST-8 | Load/performance tests for retrieval at scale. | **Open question** |
| TST-9 | Security tests (authZ, injection). | **Open question** |

### 10.1 Suggested state-machine test matrix (specified transitions)

| Test | Expected |
|------|----------|
| OPEN → IN_PROGRESS | Allow |
| IN_PROGRESS → RESOLVED | Allow |
| RESOLVED → CLOSED | Allow |
| OPEN → CANCELLED | Allow |
| IN_PROGRESS → CANCELLED | Allow |
| OPEN → OPEN | Reject |
| CLOSED → * | Reject |
| CANCELLED → * | Reject |

Classification: **Specified** (expected outcomes for listed cases); unlisted edges → **Assumption** (reject).

---

## 11. Ambiguities or missing requirements

| ID | Topic | Notes | Classification |
|----|-------|-------|----------------|
| AQ-1 | Ticket fields and workflows beyond status | No SLA, priority, or assignment rules | **Open question** |
| AQ-2 | Meaning of “ticket data” for embedding | Status text? Comments? History? | **Open question** |
| AQ-3 | No-match message format | User-facing copy, error code | **Open question** |
| AQ-4 | Citation format | Inline IDs, footnotes, links | **Open question** |
| AQ-5 | Reopen / rollback paths | RESOLVED → IN_PROGRESS? | **Open question** |
| AQ-6 | Concurrent updates | Two clients transition same ticket | **Open question** |
| AQ-7 | Identity and access control | — | **Open question** |
| AQ-8 | Deployment topology | Monolith vs. services | **Open question** |
| AQ-9 | Similarity metric and threshold scale | Cosine 0–1 vs. distance | **Open question** |
| AQ-10 | Whether `RESOLVED` → `CLOSED` is automatic or manual-only | Only edge is allowed; trigger unspecified | **Open question** |

---

## 12. Risks and assumptions

### 12.1 Risks

| ID | Risk | Impact | Classification |
|----|------|--------|----------------|
| RSK-1 | Unspecified authZ → cross-user data in RAG | High | **Open question** drives risk |
| RSK-2 | Embedding lag after ticket update → wrong answers | Medium | **Specified** refresh; **Open question** on SLA |
| RSK-3 | Mis-tuned threshold/K → no-match or irrelevant LLM context | Medium | **Specified** configurability mitigates |
| RSK-4 | Hallucination despite citation requirement | Medium | **Specified** constraints; validation TBD |
| RSK-5 | Scope creep (notifications, SLAs) without requirements | Schedule | **Open question** |
| RSK-6 | Greenfield repo — no existing contracts or data migration plan | Delivery | **Assumption** (empty workspace) |

### 12.2 Assumptions (consolidated)

| ID | Assumption |
|----|------------|
| ASM-1 | System exposes programmatic APIs for tickets and RAG (style unspecified). |
| ASM-2 | Tickets have stable string/UUID identifiers used in citations. |
| ASM-3 | Vector search is the primary retrieval mechanism unless stakeholders choose hybrid. |
| ASM-4 | “Relevant tickets” means documents surviving threshold and top-K filtering. |
| ASM-5 | Backend is the sole authority for state-machine validation. |
| ASM-6 | Configuration for K and threshold is available at runtime to retrieval service. |
| ASM-7 | A ticket persistence layer exists to source ingestion and status updates. |

### 12.3 Specified rules summary (traceability)

The following are **binding** from the project brief:

1. Status graph: `OPEN` → `IN_PROGRESS` → `RESOLVED` → `CLOSED`; plus `OPEN` → `CANCELLED` and `IN_PROGRESS` → `CANCELLED`.
2. `CLOSED` and `CANCELLED` are terminal.
3. Same-status transitions rejected; any other non-listed transition rejected by **backend**.
4. RAG answers use **only** retrieved ticket context; **cite ticket IDs** from the keep-set.
5. No relevant tickets → **explicit no-match**; **LLM not called**.
6. **Embeddings refreshed** on ticket data change.
7. **`top-K`** and **similarity threshold** are **configurable**, not hardcoded.

---

## Document control

| Field | Value |
|-------|--------|
| Version | 1.0 |
| Status | Draft for stakeholder review |
| Next step | Resolve **Open questions** before detailed API/schema design |