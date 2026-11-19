# Testing — Implementation Rules

**Type:** Checklist  
**Specs:** [spec/test-strategy.md](../spec/test-strategy.md), [spec/evaluation-strategy.md](../spec/evaluation-strategy.md), [spec/state-machine.md](../spec/state-machine.md)

---

## Default suite (`mvn test` / default Gradle test)

- [ ] **H2** in-memory database
- [ ] **Flyway off** in test profile
- [ ] **Hibernate `create-drop`** (or equivalent schema bootstrap)
- [ ] **In-memory vector store** implementation — **no PGVector** required in default CI
- [ ] **Fake / stub embedding** client — deterministic vectors (no network)
- [ ] **Fake / stub chat / generator** — scripted responses for grounding tests
- [ ] **Do not call Ollama** during default `mvn test` (no `localhost:11434` in CI path)

---

## Test layers (must exist for touched areas)

- [ ] **Domain:** `TicketStatusTransitionPolicy` — full matrix ([state-machine.md](../spec/state-machine.md) §7)
- [ ] **Service:** transitions, PATCH rules, ingest hook after success
- [ ] **MockMvc:** [api-contract.md](../spec/api-contract.md) endpoints and error codes
- [ ] **Ingestion:** chunking/fingerprint/replace with in-memory vectors
- [ ] **Retrieval:** change `rag.retrieval.top-k` / `similarity-threshold` in test config → **different** keep-set (proves no hardcoding in `RagRetrievalService`)
- [ ] **Grounding:** empty keep-set → generator **not** invoked (mock verify)
- [ ] **Frontend Vitest:** filters, status buttons, RAG outcome display ([ui-flow.md](../spec/ui-flow.md))

---

## Golden eval data

- [ ] Cases under `src/test/resources/eval/` ([evaluation-strategy.md](../spec/evaluation-strategy.md))
- [ ] **`hallucination-duration-expiry.json`**: bad 24h answer → **`NO_MATCH`**, never **`ANSWER`**
- [ ] Load golden cases without weakening production behavior ([commands/generate-tests.md](../commands/generate-tests.md))

---

## Optional integration profile (gated)

- [ ] Profile **not** active by default (e.g. `it-ollama-pg`, `@Tag("integration")`, `RUN_INTEGRATION=true`)
- [ ] May use real **PostgreSQL + PGVector** and **Ollama**
- [ ] **CI default job** does not require Docker Ollama/Postgres

---

## Forbidden in default tests

- [ ] `@Disabled` on critical policy tests to make CI green
- [ ] Lowering `similarity-threshold` in **production** code to satisfy a fixture
- [ ] Skipping post-generation gate in production for eval convenience
- [ ] `@SpringBootTest` hitting real Ollama without explicit gate

---

## Sign-off

- [ ] Default pipeline green with H2 + fakes only  
- [ ] [commands/generate-tests.md](../commands/generate-tests.md) followed for any new spec-linked test