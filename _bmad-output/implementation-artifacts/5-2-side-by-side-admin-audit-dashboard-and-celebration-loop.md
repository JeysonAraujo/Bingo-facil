# Story 5.2: Side-by-Side Admin Audit Dashboard & Celebration Loop

Status: done

## Story

As an Admin,
I want to review a side-by-side audit overlay highlighting matching numbers on a player's card compared to the actual drawn history,
so that I can approve the victory and broadcast a full-screen confetti celebration to all active players.

## Acceptance Criteria

1. **Given** a secure win claim received from a player session
2. **When** the Admin opens the win claim alert modal
3. **Then** a side-by-side visual audit panel highlights the player's card layout matched against the actual drawn numbers in chronological order
4. **When** the Admin clicks "Approve Winner"
5. **Then** a full-screen canvas confetti particle animation and victory sound chime is broadcasted instantly to all active player client rooms.

## Tasks / Subtasks

- [x] **Task 1: Admin Console Audit Modal UI (Frontend)**
  - [x] Render candidate ticket cells side-by-side with drawn balls chronological sequence.
  - [x] Highlight matches on both card cells and chronological draws for immediate verification.
- [x] **Task 2: Claims Actions Integration (Backend & Frontend)**
  - [x] Expose REST mappings `/api/v1/claims/{id}/approve` and `/api/v1/claims/{id}/reject` on the backend.
  - [x] Map admin store calls `approveClaim(id)` / `rejectClaim(id)` to backend API.
  - [x] Wire buttons in the audit modal to trigger actions and update the UI reactively.
- [x] **Task 3: WebSocket Victory Broadcasts (Backend)**
  - [x] Dispatch claims WS payload with `BINGO_APPROVED`/`BINGO_REJECTED` types to `/topic/events/{eventId}/claims`.
- [x] **Task 4: Player Victory Celebration (Frontend)**
  - [x] Listen for claim events in the game store and capture approved winner info.
  - [x] Open full-screen celebration dialog in `GameRoom.vue`.
  - [x] Run high-quality canvas confetti particles animation and trigger victory audio/synthesized chimes.

## Dev Notes

### Key Architecture & Design Decisions

**Audit Match Highlighting**:
In the side-by-side modal, matching numbers are highlighted using a distinct amber/gold background color (`#f59e0b`). This makes visual inspection extremely fast and intuitive for the event coordinator.

**Web Audio API Fallback**:
If browser autoplay policies block the victory sound file (`/victory.mp3`), a synthesized musical chord (C5 - E5 - G5 - C6) is automatically generated using browser Web Audio API `OscillatorNode` oscillators, ensuring the audio feedback never completely fails.

## File List

### Backend — Modified Files
- `bingo-backend/src/main/java/com/bingoai/backend/game/ClaimController.java` [MODIFIED — added approve/reject API endpoints]

### Frontend — Modified Files
- `bingo-frontend/src/services/websocket.ts` [MODIFIED — added claims topic subscription callback support]
- `bingo-frontend/src/stores/admin.ts` [MODIFIED — added claims array and actions to fetch/approve/reject claims]
- `bingo-frontend/src/stores/game.ts` [MODIFIED — added WS callback to show victory overlay]
- `bingo-frontend/src/components/admin/EventGameConsole.vue` [MODIFIED — added side-by-side audit dialog UI and triggers]
- `bingo-frontend/src/components/player/GameRoom.vue` [MODIFIED — added full-screen victory dialog, sound chime, and canvas confetti]

## Dev Agent Record

### Agent Model Used

Gemini 3.5 Flash (High) / Antigravity

### Validation Results
- **Backend Tests:** Passed (ClaimControllerTests verify approve/reject REST flows).
- **Frontend HMR compilation:** Compiled successfully.
