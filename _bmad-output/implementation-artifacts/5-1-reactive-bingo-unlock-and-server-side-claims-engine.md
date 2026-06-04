# Story 5.1: Reactive Bingo Unlock & Server-Side Claims Engine

Status: done

## Story

As a player on the verge of winning,
I want the "BINGO!" button to unlock dynamically and trigger a server-side audit of my matrix upon submission,
so that I can claim my prize securely and prevent client-side hacks.

## Acceptance Criteria

1. **Given** an active card that matches the active round's winning pattern (e.g. column, line, diagonal, outer border, inner border, or full card)
2. **When** the final number is marked visually
3. **Then** the BINGO button unlocks, changing from gray to pulsing gold, and programmatically shifts screen reader focus
4. **When** the player clicks the gold button
5. **Then** a secure POST win claim payload is sent to `/api/v1/claims` and verified strictly on the backend by cross-referencing the card matrix against the drawn balls history.

## Tasks / Subtasks

- [x] **Task 1: Database Claims Storage (Backend)**
  - [x] Create database migration `V5__create_claims_table.sql` exposing the `claims` table.
  - [x] Implement standard `Claim` JPA entity and `ClaimRepository` for database persistence.

- [x] **Task 2: Win Verification Engine (Backend)**
  - [x] Implement `WinVerificationEngine.java` verifying 6 patterns (LINE, COLUMN, DIAGONAL, OUTER_BORDER, INNER_BORDER, FULL_CARD).
  - [x] Support default legacy patterns (`ANY_LINE`, `ANY_COLUMN`, `FULL_GRID`).

- [x] **Task 3: Win Claims API & WebSockets (Backend)**
  - [x] Implement REST controller `ClaimController.java` exposing `POST /api/v1/claims`.
  - [x] Add session token validation matching player session vectors.
  - [x] Enforce server-side verification using `WinVerificationEngine`.
  - [x] Dispatch claims updates to WebSocket topic `/topic/events/{eventId}/claims` with rich DTO metadata (`cardIndex`, `claimId`, `status`, `serial`).

- [x] **Task 4: Dynamic UI Unlocking & Focus (Frontend)**
  - [x] Define client-side replicas of the 6 win verification checks.
  - [x] Bind BINGO claim button states to `isBingoButtonUnlocked` property, making it focusable and styling it appropriately (gray when disabled, gold/pulsing when enabled).
  - [x] Implement programmatic focus shifting to the BINGO button using Vue `watch` and template refs.

- [x] **Task 5: Admin Event Creator Options (Frontend)**
  - [x] Add Diagonal, Janelão (Outer Border), and Janelinha (Inner Border) options to `EventCreator.vue` prize lists and draw patterns definitions.
  - [x] Handle default selection changes on type selection.

- [x] **Task 6: Verification & Test Coverage**
  - [x] Create comprehensive JUnit tests in `WinVerificationTests.java` covering all six winning patterns.
  - [x] Execute backend tests successfully (`mvn test`).

## Dev Notes

### Key Architecture & Design Decisions

**Multi-Pattern Auditing**:
We engineered the `WinVerificationEngine` to support standard and complex winning patterns (Horizontal, Vertical, Diagonals, Outer edge frame, and Inner boundary cells). This is verified server-side using the round's configured draw patterns, ensuring that the backend logic is highly robust and configurable.

**A11y & Screen Reader Support**:
The BINGO claim button dynamically unlocks and shifts the browser focus programmatically once the winning pattern is satisfied, making it accessible to assistive devices. The button uses standard `:disabled` HTML attributes and contrast ratios exceeding 4.5:1.

## File List

### Backend — New Files
- `bingo-backend/src/main/resources/db/migration/V5__create_claims_table.sql` [NEW — claims table schema]
- `bingo-backend/src/main/java/com/bingoai/backend/game/Claim.java` [NEW — Claim JPA entity]
- `bingo-backend/src/main/java/com/bingoai/backend/game/ClaimRepository.java` [NEW — Claim repository]
- `bingo-backend/src/main/java/com/bingoai/backend/game/WinVerificationEngine.java` [NEW — win patterns check algorithms]
- `bingo-backend/src/main/java/com/bingoai/backend/game/ClaimController.java` [NEW — POST win claims REST API controller]
- `bingo-backend/src/main/java/com/bingoai/backend/game/dto/ClaimRequestDto.java` [NEW — request payload DTO]
- `bingo-backend/src/main/java/com/bingoai/backend/game/dto/ClaimResponseDto.java` [NEW — response wrapper DTO]
- `bingo-backend/src/test/java/com/bingoai/backend/game/WinVerificationTests.java` [NEW — unit tests for verification engine]

### Backend — Modified Files
- `bingo-backend/src/main/java/com/bingoai/backend/game/dto/BingoClaimMessage.java` [MODIFIED — enriched with claim metadata]

### Frontend — Modified Files
- `bingo-frontend/src/stores/game.ts` [MODIFIED — added eventDetails state, claimBingo api dispatch action]
- `bingo-frontend/src/components/player/GameRoom.vue` [MODIFIED — implemented client-side patterns check, BINGO button disabled toggles and focus watches]
- `bingo-frontend/src/components/admin/EventCreator.vue` [MODIFIED — added new marking patterns to selection dropdowns]

## Dev Agent Record

### Agent Model Used
Gemini 3.5 Flash (High) / Antigravity

### Validation Results
- **Backend Tests:** Passed (`mvn test` successfully completed all 14 tests, with 0 failures/errors).
- **Frontend HMR compilation:** Compiled successfully with no warnings.
