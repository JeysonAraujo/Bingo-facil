# Story 6.3: P2 Quality, Security, and Operations

Status: done

## Story

As a Development Team and Operations Engineer,
I want to implement automated continuous integration, enforce data privacy (LGPD), and guarantee system scalability under concurrent loads,
so that the platform remains stable in production, passes compliance requirements, and prevents race conditions during high-volume events.

## Acceptance Criteria

1. **Given** a GitHub repository, **When** a pull request or push is made to the main branch, **Then** a GitHub Actions workflow must automatically build the backend, check frontend vulnerabilities, and run all automated tests (including E2E).
2. **Given** a GitHub repository, **When** dependencies become outdated, **Then** Dependabot must automatically create Pull Requests on a weekly schedule.
3. **Given** the requirement to handle player data, **When** a user registers their Name and Phone, **Then** the platform must have a formally documented LGPD Privacy Policy (`docs/POLITICA_PRIVACIDADE.md`) explaining data minimization, legal basis, and the non-sharing of personal data.
4. **Given** the Playwright E2E suite, **When** tests are executed (`npm run test:e2e`), **Then** they must successfully navigate the new P1 flow (activation screen to GameRoom) and correctly interact with the Vuetify LGPD modal without timing out.
5. **Given** the "Claim" Bingo feature, **When** multiple identical requests hit the backend simultaneously, **Then** the Java concurrency test must prove that the database `UNIQUE` constraints and application logic effectively prevent duplicate claims from being created.
6. **Given** the system scalability requirements, **When** preparing for production, **Then** a k6 load-testing script must be provided to stress test the WebSocket connections (STOMP) for future execution by the infrastructure team.

## Tasks / Subtasks

- [x] **Task 1: Continuous Integration (CI)**
  - [x] Create `.github/workflows/ci.yml` defining jobs for both Backend (Maven) and Frontend (NPM/Playwright).
  - [x] Create `.github/dependabot.yml` configured for Maven and NPM ecosystems.
- [x] **Task 2: LGPD and Privacy Compliance**
  - [x] Write `docs/POLITICA_PRIVACIDADE.md` covering Data Minimization, Legal Basis, Sharing limits, and Security.
- [x] **Task 3: End-to-End (E2E) Test Updates**
  - [x] Modify `game-flow.spec.ts` to mock the new `/validate` API route.
  - [x] Update test interactions to correctly fill the Name and Phone inputs within the LGPD modal.
  - [x] Fix Vuetify `<v-checkbox>` interaction in Playwright by clicking the explicit label text (`getByText`).
- [x] **Task 4: Concurrency Testing**
  - [x] Create `ConcurrencyClaimTest.java` in the backend using `ExecutorService` and `CountDownLatch`.
  - [x] Fix backend `TicketMatrix` type casting issues in the test context.
  - [x] Verify that 10 concurrent requests result in exactly 1 success (201) and 9 rejections (DataIntegrityViolation).
- [x] **Task 5: Load Testing Preparation**
  - [x] Create `k6-load-test.js` script mimicking STOMP WebSocket `CONNECT` and `SUBSCRIBE` commands.

## Dev Notes

- This story successfully completes the **Fase P2 (Qualidade, Segurança e Operação)** as planned.
- The `ConcurrencyClaimTest` relies heavily on database constraints (`DataIntegrityViolationException`), confirming that the system is safe from basic race condition exploits.
- The load testing execution was deliberately deferred to avoid overloading the local development environment, but the script is ready for cloud deployment.
- Playwright's strict mode required careful handling of Vuetify's DOM structure; clicking the label text proved more reliable than forcing a click on the hidden `input[type="checkbox"]`.
