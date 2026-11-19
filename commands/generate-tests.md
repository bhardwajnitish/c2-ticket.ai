# Command: Generate Tests from Spec

**Purpose:** Add or extend tests traced to a **spec id** without **weakening production behavior** to make fixtures pass.

---

## Inputs

| Input | Example |
|-------|---------|
| Spec reference | `state-machine.md` §7.1 case L3, `api-contract.md` §4.5, `rag-api-contract.md` §5.1, `requirements.md` FR-9 |
| Golden file | `src/test/resources/eval/hallucination-duration-expiry.json` |
| Layer | unit / service / MockMvc / integration / Vitest |

---

## Procedure (checklist)

### 1. Traceability

- [ ] Name test or class with spec reference in comment: `// spec: state-machine.md §7.1 L3`
- [ ] One logical assertion per requirement edge where possible

### 2. Choose the right layer

| Spec area | Layer |
|-----------|--------|
| Transition matrix | `TicketStatusTransitionPolicy` unit test |
| HTTP codes / envelope | MockMvc |
| Ingest replace / fingerprint | Service + in-memory vector fake |
| RAG empty keep-set | Grounding test + generator mock **verify no interaction** |
| UI filters / RAG display | Vitest |

Do **not** use full `@SpringBootTest` + Ollama when unit/slice suffices ([test-strategy.md](../spec/test-strategy.md)).

### 3. Fixtures and doubles (allowed)

- [ ] **Stub embedding** — deterministic vector from input string hash
- [ ] **Stub generator** — return scripted `generatorOutput` from golden JSON
- [ ] **H2** data setup for tickets/comments
- [ ] **In-memory** vector store implementing same port as PGVector adapter

### 4. Forbidden shortcuts (production)

- [ ] **Do not** lower `similarity-threshold` in `application.yml` (main) for tests
- [ ] **Do not** disable post-generation gate in prod profile
- [ ] **Do not** skip policy check in service for “test convenience”
- [ ] **Do not** `@Disabled` critical spec tests without ticket + spec amendment
- [ ] **Do not** add `if (profile.equals("test"))` branches that bypass RAG rules in **main** source

### 5. Configuration in tests only

- [ ] Override `rag.retrieval.top-k` / `similarity-threshold` via `@TestPropertySource` or `application-test.yml`
- [ ] Prove config wiring by **changing properties** and asserting **different** retrieval results ([test-strategy.md](../spec/test-strategy.md))

### 6. Golden eval cases

When adding from `src/test/resources/eval/*.json`:

- [ ] Load JSON in test; drive **stubs** with `contextChunks` and `generatorOutput`
- [ ] Assert `expectOutcome`, `expectNoMatchCode`, `mustNotReturnOutcome` from file
- [ ] For `hallucination-duration-expiry.json`: assert **`NO_MATCH`**, **`answer` null**, never **`ANSWER`**

### 7. After adding tests

- [ ] Run default `mvn test` — **no Ollama**
- [ ] Update no production code unless spec demands behavior the test reveals as missing

---

## Output format (for PR description)

```markdown
### Tests added
- `ClassName#method` — spec: <doc> §<section> — verifies: <one line>

### Fakes used
- EmbeddingFake, GeneratorStub, ...

### Production changes
- None | <minimal fix aligned with spec>
```

---

## If the test cannot pass without a prod change

1. Stop and cite **spec section** that requires the behavior.
2. Run [review-spec.md](review-spec.md) if spec seems ambiguous.
3. Implement **spec-compliant** production change — not a test-only bypass.

---

## Rules of engagement

- Tests document **contract**; they are not a second product spec.
- Prefer parameterized tests for state machine edges and policy tables.
- Frontend tests mock `POST /api/v1/rag/query` with envelopes from [rag-api-contract.md](../spec/rag-api-contract.md) examples.