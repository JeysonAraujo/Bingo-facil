# Story 3.2: Error-Proof 75-Key Admin Draw Board & Undo Safeguard

Status: review

## Story

As an Event Coordinator,
I want a visual 75-key grid that requires double-click confirmation or keyboard Enter confirmation to draw numbers, with a prominent "UNDO" button,
so that I can record drawn numbers accurately without accidental broadcasts.

## Acceptance Criteria

1. **Given** an active event round in progress
2. **When** I click a number on the Admin grid or use hotkey shortcuts
3. **Then** the visual key turns flashing yellow (pending state)
4. **When** I click it a second time within 500ms or press Enter
5. **Then** the ball number is persisted in the database and broadcasted instantly to the WebSocket draws topic
6. **And** clicking the red 'UNDO' button immediately deletes the last draw from the database and broadcasts a retraction event to all players.

## Tasks / Subtasks

- [x] **Task 1: Database & Persistence Layer (Backend)**
  - [x] Create Flyway database migration `V2__add_draws_table.sql`
  - [x] Create `Draw.java` JPA Entity in `com.bingoai.backend.game`
  - [x] Create `DrawRepository.java` in `com.bingoai.backend.game`
  - [x] Create `RoundRepository.java` in `com.bingoai.backend.event`

- [x] **Task 2: Service & Controller Layer (Backend)**
  - [x] Implement `GameService.java` for business logic (recording draws, retracting draws, fetching history)
  - [x] Modify `GameWebSocketController.java` to persist draws and handle `@MessageMapping("/events/{eventId}/undo")`
  - [x] Implement `GameController.java` for `GET /api/v1/events/{eventId}/draws` REST API

- [x] **Task 3: Admin Console & Router Setup (Frontend)**
  - [x] Register console path `/admin/events/:eventId/console` in `router/index.ts`
  - [x] Extend Pinia `admin.ts` store with console state, drawing actions, and WebSocket connections
  - [x] Modify `AdminDashboard.vue` to enable routing via "Gerenciar Evento" button
  - [x] Create `EventGameConsole.vue` (75-key grid, double-click guard, keyboard hotkeys, RED UNDO button, latency slider)

- [x] **Task 4: Player Synchronization & WebSocket updates (Frontend)**
  - [x] Update `websocket.ts` service to parse `BALL_RETRACTED` message envelopes and trigger callback
  - [x] Update Pinia `game.ts` store with `fetchInitialDraws()` and `retractDrawnNumber()` actions
  - [x] Modify `GameRoom.vue` to invoke `fetchInitialDraws()` inside `onMounted` before WS connection

- [x] **Task 5: Compile, Build & Verify**
  - [x] Run `mvn compile` on backend to ensure zero compile errors and database mapping safety
  - [x] Run `npm run type-check` on frontend to guarantee complete TypeScript strict safety
  - [x] Run `npm run build-only` to ensure ESM bundling is flawless

## Dev Notes

### Key Architecture & Design Decisions

**State Recovery via HTTP REST Endpoint:**
To support absolute reliability (NFR4) and recover state after browser refreshes, we implemented a REST endpoint `GET /api/v1/events/{eventId}/draws`. Player clients hit this endpoint on component mount to retrieve the chronological history of drawn balls, updating local game store matrices retroactively before subscribing to real-time WebSockets.

**WebSocket Retraction Broadcast:**
Undo actions delete the last draw from the database and broadcast a `BALL_RETRACTED` event. The Vue player client catches this event type and immediately retracts the corresponding ball number from the reative store state, ensuring both screens are perfectly synchronized in real-time.

**Advanced Administrative Hotkeys & double-click protection:**
Coordinators can select balls by double-clicking them within 500ms on the grid, or completely hands-free by typing the ball number, pressing `Enter` to confirm, `Esc` to clear selection, or `Ctrl+Z` to trigger an undo operation.

## File List

### Backend — New Files
- `bingo-backend/src/main/resources/db/migration/V2__add_draws_table.sql` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/game/Draw.java` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/game/DrawRepository.java` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/game/GameService.java` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/game/GameController.java` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/event/RoundRepository.java` [NEW]

### Backend — Modified Files
- `bingo-backend/src/main/java/com/bingoai/backend/game/GameWebSocketController.java` [MODIFIED — integrated GameService draws persistence and undo WS handler]
- `bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketActivationResponseDto.java` [MODIFIED — added eventId field for player console routing]
- `bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketActivationService.java` [MODIFIED — populated eventId inside activation response]

### Frontend — New Files
- `bingo-frontend/src/components/admin/EventGameConsole.vue` [NEW]

### Frontend — Modified Files
- `bingo-frontend/src/router/index.ts` [MODIFIED — registered /admin/events/:eventId/console path]
- `bingo-frontend/src/stores/admin.ts` [MODIFIED — extended with console states and WS draw/undo publish methods]
- `bingo-frontend/src/stores/game.ts` [MODIFIED — extended with fetchInitialDraws and retractDrawnNumber actions]
- `bingo-frontend/src/services/websocket.ts` [MODIFIED — updated createWebSocketClient signature to parse BALL_RETRACTED signals]
- `bingo-frontend/src/components/player/GameRoom.vue` [MODIFIED — retrieved initial history from database before connecting via WebSocket]
- `bingo-frontend/src/components/admin/AdminDashboard.vue` [MODIFIED — enabled Gerenciar Evento routing button]
- `bingo-frontend/src/services/api.ts` [MODIFIED — added eventId to TicketActivationResponseDto TS interface]

## Dev Agent Record

### Agent Model Used
Gemini 3.5 Flash (High) / Antigravity

### Validation Results
- **Maven Clean Compile:** Passed successfully with zero errors (BUILD SUCCESS).
- **TypeScript Check:** `npm run type-check` finished successfully with no strict types errors.
- **Production Build:** `npm run build-only` bundled all assets smoothly in 5.66s, compiling the new chunks and stylesheets flawlessly.
