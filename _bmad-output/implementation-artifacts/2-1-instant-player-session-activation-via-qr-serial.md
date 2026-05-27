# Story 2.1: Instant Player Session Activation via QR / Serial

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a physical ticket holder,
I want to scan my card's QR Code or type the 6-character serial code on a web landing page,
so that I can instantly activate my ticket and join the digital game room without setting up a traditional username/password account.

## Acceptance Criteria

1. **Given** a valid unactivated ticket serial persisted in the database
2. **When** I visit the ticket's embedded QR activation URL (e.g., `http://localhost:3000/activate?serial=XXXXXX`) or type the 6-character serial manually in the input field
3. **Then** the system validates the serial, marks the ticket status as `activated = true` in the database, and generates a temporary secure `sessionToken` (UUID) stored in the `tickets.session_token` column
4. **And** the backend returns a response containing the `sessionToken` and the ticket's full matrix data (the 4 bingo cards)
5. **And** the frontend stores the `sessionToken` in `localStorage` and redirects the player smoothly into the game room at `/game`
6. **Given** an already-activated ticket serial
7. **When** I attempt to activate it again
8. **Then** the system returns an error response with a meaningful message (e.g., "Esta cartela já foi ativada.")
9. **Given** a non-existent or invalid serial
10. **When** I attempt to activate it
11. **Then** the system returns a 404 error with a meaningful message (e.g., "Cartela não encontrada.")
12. **And** the activation page auto-submits when exactly 6 characters are entered in the serial input field (no explicit submit button required)
13. **And** the activation page reads the `?serial=` query parameter on mount and auto-triggers activation if present
14. **And** the game room at `/game` redirects to `/activate` if no `sessionToken` is found in `localStorage`

## Tasks / Subtasks

- [x] **Task 1: Backend — Activation Endpoint** (AC: 1, 2, 3, 4, 6, 7, 8, 9, 10, 11)
  - [x] Add `findBySerial(String serial)` method to `TicketRepository`.
  - [x] Create `TicketActivationService.java` in `com.bingoai.backend.ticket` with a transactional `activateTicket(String serial)` method:
    - Look up ticket by serial → throw `ResponseStatusException(404)` if not found.
    - If `activated == true` → throw `ResponseStatusException(409, "Esta cartela já foi ativada.")`.
    - Otherwise: set `activated = true`, generate a `UUID.randomUUID().toString()` as `sessionToken`, save, and return.
  - [x] Create `TicketActivationController.java` in `com.bingoai.backend.ticket`:
    - `POST /api/v1/tickets/activate` — accepts `{ "serial": "XXXXXX" }` body, returns standard `ApiResponse<TicketActivationResponseDto>`.
  - [x] Create `TicketActivationResponseDto.java` in `com.bingoai.backend.ticket` containing: `ticketId` (UUID), `sessionToken` (String), `serial` (String), `matrix` (TicketMatrix).
  - [x] Create `ActivateTicketRequest.java` DTO with `@NotBlank` validation on `serial` field.

- [x] **Task 2: Frontend — Activation Page** (AC: 2, 5, 12, 13)
  - [x] Create `TicketActivation.vue` in `bingo-frontend/src/components/player/`.
  - [x] Read `?serial=` query param on mount using `useRoute()` — if present, auto-trigger activation immediately.
  - [x] Render a centered, accessible input field with `autofocus`, `maxlength="6"`, placeholder `"Ex: ABC123"` that auto-uppercases input.
  - [x] Watch the input value: when it reaches 6 characters, automatically call the activation action (no button needed).
  - [x] Show inline loading spinner during the API call.
  - [x] Show clear error message below the input on failure.
  - [x] On success: store `sessionToken` in `localStorage` under key `bingo_session_token`, store `ticketMatrix` in `localStorage` under key `bingo_ticket_matrix`, then `router.push('/game')`.

- [x] **Task 3: Frontend — Game Room Placeholder** (AC: 5, 14)
  - [x] Create `GameRoom.vue` in `bingo-frontend/src/components/player/` as a placeholder page for the game room (the full implementation is in Story 2.2).
  - [x] On mount, check if `localStorage.getItem('bingo_session_token')` exists — if not, immediately redirect to `/activate`.
  - [x] Display a "Bem-vindo ao Bingo!" welcome screen showing the player's serial/session info.

- [x] **Task 4: Frontend — Routing & Store** (AC: 5, 14)
  - [x] Add `/activate` and `/game` routes to `bingo-frontend/src/router/index.ts`.
  - [x] Create `game.ts` Pinia store in `bingo-frontend/src/stores/` to hold `sessionToken` and `ticketMatrix` reactively (loaded from `localStorage` on init).
  - [x] Add the `activateTicket(serial: string)` action to the game store that calls `POST /api/v1/tickets/activate` and updates state.
  - [x] Add the `TicketActivationResponseDto` and `ActivateTicketRequest` interfaces to `bingo-frontend/src/services/api.ts`.

- [x] **Task 5: Testing** (AC: 1, 2, 3, 6, 9, 11)
  - [x] Create `TicketActivationControllerTest.java` in `bingo-backend/src/test/java/com/bingoai/backend/ticket/` using Spring MockMvc.
  - [x] Test: successful activation of a valid unactivated serial → 200 with `sessionToken`.
  - [x] Test: activation of already-activated serial → 409.
  - [x] Test: activation of a non-existent serial → 404.
  - [x] Test: activation with blank serial → 400 validation error.

### Review Findings

- [x] [Review][Patch] Center FREE Star CSS Styling [bingo-frontend/src/components/player/GameRoom.vue:59]
- [x] [Review][Patch] Null Reason Fallback in GlobalExceptionHandler [bingo-backend/src/main/java/com/bingoai/backend/common/exception/GlobalExceptionHandler.java:31]
- [x] [Review][Patch] Concurrency Race Condition on Ticket Activation [bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketActivationService.java:20]

## Dev Notes

### Backend Architecture
- **Package**: All new backend classes go in `com.bingoai.backend.ticket` (package-by-feature pattern). [Source: architecture.md#Project-Organization]
- **Endpoint Pattern**: `POST /api/v1/tickets/activate` following plural kebab-case REST convention. [Source: architecture.md#API-Naming-Conventions]
- **Response Envelope**: All responses wrapped in `ApiResponse<T>` from `com.bingoai.backend.common.dto`. [Source: architecture.md#API-Response-Formats]
- **Exception Handling**: Use `ResponseStatusException` for 404/409 errors — the existing `GlobalExceptionHandler` will translate these into the standard `ApiError` envelope automatically. [Source: existing GlobalExceptionHandler in common/exception]
- **Transaction**: `activateTicket()` must be `@Transactional` to ensure atomicity of the read-modify-write cycle.
- **Security**: Current `SecurityConfig` permits all requests (`anyRequest().permitAll()`). The new endpoint is public by design — ticket activation is password-free by architecture. [Source: architecture.md#Authentication-&-Security]
- **Session Token**: A UUID string is sufficient for this MVP session token (no JWT needed for Story 2.1). JWT integration can be added in a later story if WebSocket authentication requires it.

### Database
- **No migration needed**: The `tickets` table already has `session_token VARCHAR(255)` and `activated BOOLEAN` columns from `V1__init_schema.sql`. [Source: existing V1__init_schema.sql]
- **Index**: `idx_tickets_serial` index already exists on `tickets(serial)`, ensuring fast lookups. [Source: existing V1__init_schema.sql]
- **Existing `TicketRepository`**: Must add `findBySerial(String serial)` — Spring Data JPA will derive the query automatically.

### Frontend Architecture
- **Component Location**: Player components go in `src/components/player/`. This folder does NOT exist yet — it must be created. [Source: architecture.md#Frontend-Layout]
- **State Management**: Use Pinia `game.ts` store (listed in architecture but not yet created). The store must initialize from `localStorage` on load so session survives page refresh.
- **Router Guard**: The `/game` route should check for `sessionToken`; redirect to `/activate` if missing. Implement this as a simple in-component `onMounted` check (full navigation guards can be added later).
- **API URL**: Axios base URL is `http://localhost:8080/api/v1` as configured in `api.ts`. The new endpoint path is `/tickets/activate`.
- **UX Pattern — Auto-Submit**: Per UX spec, the input auto-submits on 6 characters — no secondary "Enviar" button required. Use a `watch` on the input value. [Source: ux-design-specification.md#Form-Patterns-&-Ticket-Validation]
- **QR Code URL format**: QR codes generated in Story 1.3 point to `{activationBaseUrl}/activate?serial=XXXXXX`. The frontend activation page must read this `?serial` query param on mount and auto-activate. [Source: Story 1.3 PdfGenerator implementation]

### Learnings from Story 1.3
- **LazyInit pattern**: When the activation response includes ticket data, fetch everything needed within the `@Transactional` method to avoid `LazyInitializationException`. The `TicketMatrix` is stored as JSONB and loaded via `TicketMatrixConverter` — it is eagerly available from the entity.
- **TicketMatrix structure**: `TicketMatrix` contains a `List<CardMatrix>` (4 cards), each with `List<Integer>` for columns B, I, N, G, O. The `TicketActivationResponseDto` should serialize the full matrix.
- **Serial format**: Serials are 6-character uppercase alphanumeric, avoiding ambiguous chars (0, O, I, 1, L). They are stored in the `tickets.serial` column.

### Existing Files to Modify
- `TicketRepository.java` → Add `Optional<Ticket> findBySerial(String serial)`.
- `bingo-frontend/src/router/index.ts` → Add `/activate` and `/game` routes.
- `bingo-frontend/src/services/api.ts` → Add `TicketActivationResponseDto` and `ActivateTicketRequest` interfaces.

### New Files to Create
**Backend:**
- `ActivateTicketRequest.java` (DTO)
- `TicketActivationResponseDto.java` (DTO)
- `TicketActivationService.java` (Service)
- `TicketActivationController.java` (Controller)
- `TicketActivationControllerTest.java` (Test)

**Frontend:**
- `src/components/player/TicketActivation.vue`
- `src/components/player/GameRoom.vue`
- `src/stores/game.ts`

### Project Structure Notes
- Backend package-by-feature is strictly enforced. All activation code lives in `com.bingoai.backend.ticket`. [Source: architecture.md]
- Frontend `src/components/player/` must be created (it doesn't exist yet).
- The `game.ts` Pinia store is listed in the architecture's planned structure but hasn't been created yet.

### References
- [Source: _bmad-output/planning-artifacts/epics.md — Story 2.1]
- [Source: _bmad-output/planning-artifacts/architecture.md — Authentication & Security, API Naming, Project Structure]
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md — Form Patterns & Ticket Validation, Player Journey]
- [Source: bingo-backend/src/main/resources/db/migration/V1__init_schema.sql]
- [Source: bingo-backend/src/main/java/com/bingoai/backend/ticket/Ticket.java]
- [Source: bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketRepository.java]
- [Source: bingo-backend/src/main/java/com/bingoai/backend/common/config/SecurityConfig.java]
- [Source: bingo-frontend/src/router/index.ts]
- [Source: bingo-frontend/src/services/api.ts]
- [Source: bingo-frontend/src/stores/admin.ts]

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.6 (Thinking)

### Debug Log References

- **Falha de contexto de testes (H2 vs PostgreSQL jsonb):** O perfil `dev` usava `ddl-auto: validate`, mas o Flyway cria a coluna `matrix` como `jsonb` (PostgreSQL-specific). O H2 cria como `json` (Types#OTHER), causando falha de schema-validation. Solução: alterado para `ddl-auto: create-drop` + `flyway.enabled: false` no perfil `dev`.

### Completion Notes List

- ✅ Backend: `POST /api/v1/tickets/activate` implementado, validado e testado (4/4 testes passando).
- ✅ Backend: `GlobalExceptionHandler` atualizado com handler para `ResponseStatusException` (404/409 agora retornam status correto).
- ✅ Backend: `application-dev.yml` corrigido para `create-drop` + Flyway desabilitado no perfil dev, resolvendo incompatibilidade H2/PostgreSQL.
- ✅ Frontend: Store `game.ts` criada com persistência em localStorage e recovery automático na recarga da página.
- ✅ Frontend: `TicketActivation.vue` com auto-submit em 6 chars, auto-trigger via `?serial=` URL param, loading spinner e feedback de erro.
- ✅ Frontend: `GameRoom.vue` com guard de sessão, preview das 4 cartelas e botão de logout.
- ✅ Frontend: Compilado sem erros (`npm run build`).

### File List

**Backend (bingo-backend):**
- `src/main/java/com/bingoai/backend/ticket/ActivateTicketRequest.java` [NEW]
- `src/main/java/com/bingoai/backend/ticket/TicketActivationResponseDto.java` [NEW]
- `src/main/java/com/bingoai/backend/ticket/TicketActivationService.java` [NEW]
- `src/main/java/com/bingoai/backend/ticket/TicketActivationController.java` [NEW]
- `src/main/java/com/bingoai/backend/ticket/TicketRepository.java` [MODIFIED — added findBySerial]
- `src/main/java/com/bingoai/backend/common/exception/GlobalExceptionHandler.java` [MODIFIED — added ResponseStatusException handler]
- `src/main/resources/application-dev.yml` [MODIFIED — create-drop + flyway.enabled: false]
- `src/test/java/com/bingoai/backend/ticket/TicketActivationControllerTest.java` [NEW]

**Frontend (bingo-frontend):**
- `src/stores/game.ts` [NEW]
- `src/components/player/TicketActivation.vue` [NEW]
- `src/components/player/GameRoom.vue` [NEW]
- `src/router/index.ts` [MODIFIED — added /activate and /game routes]
- `src/services/api.ts` [MODIFIED — added activation DTOs/interfaces]

**Docs:**
- `_bmad-output/implementation-artifacts/2-1-instant-player-session-activation-via-qr-serial.md` [NEW]
- `_bmad-output/implementation-artifacts/sprint-status.yaml` [MODIFIED]
