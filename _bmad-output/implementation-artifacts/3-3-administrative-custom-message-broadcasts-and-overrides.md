# Story 3.3: Administrative Custom Message Broadcasts & Overrides

Status: done

## Story

As an Event Coordinator,
I want to draft and broadcast custom messages to all players' screens and search and manually mark/unmark numbers on active tickets,
so that I can coordinate announcements and override card cells directly in case of physical errors.

## Acceptance Criteria

1. **Given** an active event in progress
2. **When** I type an announcement text in the Admin Console and click Broadcast
3. **Then** a scrolling marquee banner renders dynamically at the top of every player's game room (accessible high-contrast text with a close button)
4. **When** I search a player ticket by its unique 6-character serial code
5. **Then** I see its active 5x5 card layout visualizer
6. **When** I click a cell to force-override its marking (mark or unmark)
7. **Then** the forced override is persisted in the database and dispatched dynamically over a targeted WebSocket channel solely to that player client, updating their card visual marks instantly.

## Tasks / Subtasks

- [x] **Task 1: Database & Model Layer (Backend)**
  - [x] Create Flyway database migration `V3__add_ticket_overrides.sql` to add JSONB column
  - [x] Create `TicketOverridesConverter.java` JPA Attribute Converter in `com.bingoai.backend.ticket`
  - [x] Modify `Ticket.java` JPA Entity to map the `markedOverrides` JSONB collection

- [x] **Task 2: Service & Controller Layer (Backend)**
  - [x] Implement `AdminOverrideService.java` for searching active tickets and toggling forced overrides
  - [x] Create `AdminTicketController.java` to expose endpoint `GET /api/v1/tickets/search?serial={serial}`
  - [x] Update `GameWebSocketController.java` to handle announcement broadcast and `@MessageMapping("/tickets/{ticketId}/override")` targeted dispatches
  - [x] Create WebSocket DTOs `AnnouncementMessage.java`, `TicketOverrideCommand.java`, and `TicketOverrideMessage.java`

- [x] **Task 3: Admin Console & Store Setup (Frontend)**
  - [x] Extend Pinia `admin.ts` store with serial searching actions, and announcement/override publish methods
  - [x] Expand `EventGameConsole.vue` (Announcement Broadcaster input, Ticket Search bar, 5x5 active Ticket Viewer, interactive override grid toggle)

- [x] **Task 4: Player Ticker & Overrides Integration (Frontend)**
  - [x] Update `websocket.ts` service to subscribe players to individual overrides topic and global announcements topic
  - [x] Extend Pinia `game.ts` store with `ticketId`, `overriddenNumbers`, and `activeAnnouncement` states, initial sync action `fetchTicketOverrides(serial)`, and WebSocket handlers
  - [x] Modify `GameRoom.vue` to render the marquee Scrolling Announcement Ticker under the navbar, and wire WebSocket subscription callbacks

- [x] **Task 5: Compile, Build & Verify**
  - [x] Run `mvn clean compile` on backend to ensure absolute compilation and JPA mapping safety
  - [x] Run `npm run type-check` on frontend to guarantee TypeScript strict safety
  - [x] Run `npm run build-only` to verify flawless ESM bundling

## Dev Notes

### Key Architecture & Design Decisions

**Targeted WebSocket Overrides:**
To isolate player scopes and avoid high bandwidth usage, each player client has an individual WebSocket topic subscription: `/topic/tickets/{ticketId}/overrides`. Overrides are published solely to that ticket's channel.

**Accessible Scrolling Announcement Marquee Ticker:**
We designed a horizontal scrolling marquee under the player navbar with premium CSS keyframe transitions. Using vibrant gold text (`#fef08a`) on an indigo-purple gradient (`#1e1b4b` / `#311042`), the marquee complies with WCAG AAA accessibility contrast guidelines, and includes a quick dismiss button.

**Dual-Sync Initialization Handshake:**
When a player loads or refreshes their room, the frontend store fetches their initial drawn numbers history via draws REST endpoint and queries their current ticket overrides list via search REST API to synchronize the card grid prior to initiating the WebSocket STOMP listener connection.

## File List

### Backend — New Files
- `bingo-backend/src/main/resources/db/migration/V3__add_ticket_overrides.sql` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketOverridesConverter.java` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketResponseDto.java` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/game/AdminOverrideService.java` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/ticket/AdminTicketController.java` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/game/dto/AnnouncementMessage.java` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/game/dto/TicketOverrideCommand.java` [NEW]
- `bingo-backend/src/main/java/com/bingoai/backend/game/dto/TicketOverrideMessage.java` [NEW]

### Backend — Modified Files
- `bingo-backend/src/main/java/com/bingoai/backend/ticket/Ticket.java` [MODIFIED — mapped markedOverrides JSONB collection]
- `bingo-backend/src/main/java/com/bingoai/backend/game/GameWebSocketController.java` [MODIFIED — integrated announcements broadcast and ticket-specific cell override handlers]

### Frontend — Modified Files
- `bingo-frontend/src/services/websocket.ts` [MODIFIED — added WsAnnouncementPayload and WsTicketOverridePayload, and registered real-time subscription callbacks]
- `bingo-frontend/src/stores/admin.ts` [MODIFIED — extended with ticket search actions and WebSocket announcement/override publish methods]
- `bingo-frontend/src/stores/game.ts` [MODIFIED — extended Pinia store with ticketId, overriddenNumbers, and activeAnnouncement states and actions]
- `bingo-frontend/src/components/admin/EventGameConsole.vue` [MODIFIED — fully redesigned console with announcements logs tab and search overrides tab]
- `bingo-frontend/src/components/player/GameRoom.vue` [MODIFIED — added scrolling marquee component ticker in templates/styles, and bound computed overrides to cells]

## Dev Agent Record

### Agent Model Used
Gemini 3.5 Flash (High) / Antigravity

### Validation Results
- **Maven Clean Compile:** Passed successfully with zero errors (BUILD SUCCESS).
- **TypeScript Check:** `npm run type-check` finished successfully with no strict types errors.
- **Production Build:** `npm run build-only` bundled all assets smoothly, compiling the new chunks and stylesheets flawlessly.

### Review Findings

- [x] [Review][Patch] Clean review — all layers passed.
