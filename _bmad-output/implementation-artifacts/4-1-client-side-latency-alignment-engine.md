# Story 4.1: Client-Side Latency Alignment Engine

Status: done

## Story

As a player,
I want incoming WebSocket draw events to be queued and visually marked after a delay corresponding to the stream latency,
so that my digital cartels mark numbers exactly when they are announced in the YouTube live video.

## Acceptance Criteria

1. **Given** an Admin-configured stream delay of D seconds (e.g., 10 seconds)
2. **When** a WS `BALL_DRAWN` event is received at system timestamp T
3. **Then** the client Pinia store intercepts and holds the event in an internal scheduler queue
4. **And** the number is only committed and visually marked on the player's card at exactly timestamp T + D.

## Tasks / Subtasks

- [x] **Task 1: Database & Persistence Layer (Backend)**
  - [x] Create Flyway migration script `V4__add_stream_delay_to_events.sql`
  - [x] Update `Event.java` entity with new `streamDelaySeconds` field and accessors
  - [x] Update `EventResponseDto.java` to map stream delay

- [x] **Task 2: Backend Core Changes (Backend)**
  - [x] Update `DrawBallMessage.java` constructor to accept `Instant` timestamp
  - [x] Create `StreamDelayMessage.java` DTO
  - [x] Update `GameService.java` to pass the entity's `createdAt` timestamp, support delay persistence, and handle WebSocket broadcasts

- [x] **Task 3: Backend Controller & WebSockets (Backend)**
  - [x] Modify `GameController.java` to expose REST endpoint `PUT /api/v1/events/{eventId}/delay`
  - [x] Add method in `GameWebSocketController.java` to broadcast delay updates

- [x] **Task 4: Frontend State & Services Setup (Frontend)**
  - [x] Update `websocket.ts` interfaces and subscribers for delay updates and timestamp mapping
  - [x] Update Pinia `game.ts` store to implement the scheduler delay queue
  - [x] Update Pinia `admin.ts` store with `updateStreamDelay` action
  - [x] Update `GameRoom.vue` component to fetch event details on mount

- [x] **Task 5: Frontend UI Console (Frontend)**
  - [x] Add interactive, debounced latency slider in `EventGameConsole.vue`

- [x] **Task 6: Verification & Build**
  - [x] Verify backend compiles and tests pass successfully
  - [x] Verify frontend builds and strict typechecks pass successfully

## Dev Notes

### Key Architecture & Design Decisions

**Historical Synchronization**:
To support players who join or refresh the game page in the middle of a round, we refactored the backend message envelopes (`DrawBallMessage`) to use the database-persisted creation timestamp of each draw instead of dynamic generation. When the player client fetches these historical draws via REST API upon initialization, it feeds them through the exact same alignment queue, subtracting elapsed time to mark them after the correct remaining delay.

**Memory leak prevention**:
Active timeouts are stored inside the `_latencyTimeouts` map. To prevent ghost markings of retracted numbers or memory leaks, we clear all active timers whenever a ball is retracted (`retractDrawnNumber`) or the client leaves the game room (`disconnectFromGame`).

## File List

### Backend — New Files
- `bingo-backend/src/main/resources/db/migration/V4__add_stream_delay_to_events.sql` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/game/dto/StreamDelayMessage.java` [NEW]

### Backend — Modified Files
- `bingo-backend/src/main/java/com/bingoai/backend/event/Event.java` [MODIFIED]
- `bingo-backend/src/main/java/com/bingoai/backend/event/dto/EventResponseDto.java` [MODIFIED]
- `bingo-backend/src/main/java/com/bingoai/backend/event/EventService.java` [MODIFIED]
- `bingo-backend/src/main/java/com/bingoai/backend/game/dto/DrawBallMessage.java` [MODIFIED]
- `bingo-backend/src/main/java/com/bingoai/backend/game/GameService.java` [MODIFIED]
- `bingo-backend/src/main/java/com/bingoai/backend/game/GameController.java` [MODIFIED]
- `bingo-backend/src/main/java/com/bingoai/backend/game/GameWebSocketController.java` [MODIFIED]

### Frontend — Modified Files
- `bingo-frontend/src/services/api.ts` [MODIFIED]
- `bingo-frontend/src/services/websocket.ts` [MODIFIED]
- `bingo-frontend/src/stores/game.ts` [MODIFIED]
- `bingo-frontend/src/stores/admin.ts` [MODIFIED]
- `bingo-frontend/src/components/player/GameRoom.vue` [MODIFIED]
- `bingo-frontend/src/components/admin/EventGameConsole.vue` [MODIFIED]

## Dev Agent Record

### Agent Model Used
Gemini 3.5 Flash (High) / Antigravity

### Validation Results
- **Maven Clean Compile:** Passed.
- **Backend Tests:** All tests passed (8/8).
- **TypeScript Check:** Passed with no errors.
