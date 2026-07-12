# Story 6.2: P1 Stabilization and UX Improvements

Status: done

## Story

As an Event Coordinator and Player,
I want the platform to have a stable, intuitive user experience and secure dependency management,
so that I can easily navigate the game, avoid accidental actions, and trust the integrity of the application.

## Acceptance Criteria

1. **Given** a player accessing the app, **When** they land on the initial screen, **Then** they only need to provide the Ticket Serial (or scan a QRCode) and the serial is proactively validated.
2. **Given** a valid ticket serial, **When** the player enters the GameRoom, **Then** a central modal must prompt for Name, Phone (with Brazilian mask), and LGPD consent, blocking access to the board until completed.
3. **Given** the Admin Dashboard, **When** an admin manages an event, **Then** they must use direct Action Buttons (Iniciar Evento, Finalizar Evento) with confirmation dialogs instead of a select dropdown.
4. **Given** the "Claim" (Gritar Bingo) button, **When** a user attempts to click it, **Then** a debounce mechanism must prevent spamming and the button should only activate when a bingo is actually detected.
5. **Given** backend APIs handling Event and Round statuses, **When** string values are received, **Then** they are correctly mapped to Java Enums (`EventStatus`, `RoundStatus`, `PrizeType`) without compilation or runtime errors.
6. **Given** database migrations, **When** running against H2 (Test) or PostgreSQL (Prod), **Then** migration scripts must be fully compatible (e.g., removing `DELETE USING` syntax).
7. **Given** frontend dependencies, **When** checked for security vulnerabilities (`npm audit`), **Then** there must be no high/critical vulnerabilities.
8. **Given** the backend build process, **When** running tests, **Then** Maven Wrapper (`mvnw`) must be available and all tests must pass (including the mocked `AuditLogService`).

## Tasks / Subtasks

- [x] **Task 1: Frontend UX (Player Flow)**
  - [x] Simplify TicketActivation to request only the Serial.
  - [x] Add `GET /api/v1/tickets/validate?serial={serial}` endpoint to validate ticket existence and event state.
  - [x] Implement a blocking central `v-dialog` modal in `GameRoom.vue` for Name, Phone, and LGPD consent.
  - [x] Apply debounce and bingo detection locks to the Claim button.
- [x] **Task 2: Frontend UX (Admin Dashboard)**
  - [x] Replace `v-select` status dropdowns with dynamic Action Buttons.
  - [x] Add `v-dialog` confirmation prompts for starting and finishing events.
- [x] **Task 3: Backend Stability & Testing**
  - [x] Refactor `EventService` and `RoundController` to use `Enum.valueOf()` for string-to-enum mappings.
  - [x] Fix Spring ApplicationContext test failures by adding `@MockBean` for `AuditLogService` in `ClaimControllerTests`.
  - [x] Add Maven Wrapper (`.mvnw`) to the backend repository.
- [x] **Task 4: Database & Migrations**
  - [x] Patch `V11__add_player_details_and_security_constraints.sql` to replace PostgreSQL-specific `DELETE USING` with `WHERE id IN (...)` for H2 compatibility.
- [x] **Task 5: Security & Dependencies**
  - [x] Resolve `form-data` high severity vulnerability reported by `npm audit`.
  - [x] Run `npm audit fix` and verify `0 vulnerabilities` on frontend packages.

## Dev Notes

- This story implements the **Fase P1 (Estabilização e UX)** as defined by the project requirements and audit feedback.
- The UX improvements directly address LGPD compliance by explicitly requesting consent before allowing gameplay.
- Backend tests now pass 100% on the H2 memory database thanks to the migration compatibility fix and proper Context mocking.
- The `application.yml` and `AdminSeeder` P0 issues noted earlier were considered "done" in the context of Story 6.1 by the previous agent implementation, although strict removal in dev environments was debated. We proceed as defined by the BMAD records.
