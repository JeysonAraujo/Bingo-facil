# Story 4.3: Mobile Mini-Map Selector & Glowing "Quase Lá!" Alerts

Status: done

## Story

As a mobile player,
I want a horizontal mini-map that flashes for off-screen draws and glows gold when any card is 1 number away from winning,
so that I can keep track of all 4 cards and switch focus to near-win cards in time.

## Acceptance Criteria

1. **Given** a player room loaded in a mobile viewport (carousel view)
2. **When** a card that is currently off-screen receives a new mark
3. **Then** its corresponding `CardMiniMap` thumbnail flashes green and shows a `+1` bubble for 1.5 seconds
4. **When** any card reaches a near-win state (exactly 1 number remaining to win the round prize)
5. **Then** the thumbnail glows with a pulsing gold border (`#FFD600`), and a sliding banner alerts: `"Card 3 is almost there! Only 1 number left!"`
6. **And** tapping the banner slides the carousel to focus on Card 3 automatically.

## Tasks / Subtasks

- [x] **Task 1: Track Marked Counts & Off-screen Flashes (Frontend)**
  - [x] Define reactive states `prevMarkedCounts`, `cardFlashes` and `nearWinAlertCardIndex` in `GameRoom.vue`
  - [x] Implement marked numbers watcher to detect when off-screen cards receive new marks, setting flash state for 1.5 seconds

- [x] **Task 2: Near-Win Calculation & Watcher (Frontend)**
  - [x] Implement computed property `nearWinCards` checking for cards with exactly 23 marks
  - [x] Implement watcher on `nearWinCards` to trigger the near-win banner alert when a card reaches the near-win threshold

- [x] **Task 3: Layout & Transitions (Frontend)**
  - [x] Modify mini-map buttons in `GameRoom.vue` to add `dot-flash-green` and `dot-near-win` classes dynamically, and render the `+1` bubble badge
  - [x] Implement the `near-win-banner-alert` Vue template markup above the mobile carousel
  - [x] Define transitions `slide-y` and `bounce` in CSS along with gold pulsing glow effects and green flashes

- [x] **Task 4: Compilation, Typing & Verification**
  - [x] Run `npm run type-check` to verify complete type safety and validate the frontend bundle

## Dev Notes

### Key Architecture & Design Decisions

**Reactive Off-screen Tracking**:
We track marked counts by comparing current counts against a cached list of previous counts. The watcher detects updates from both live drawings and manual admin overrides, triggering the flash green indicator and `+1` bubble strictly for cards that are off-screen (`activeMobileCardIndex !== idx`).

**Gold Pulsing & Accessibility**:
Near-win cards are styled with `#FFD600` gold gradients and borders pulsing infinitely (`pulse-border`). This provides high contrast against the dark background, quickly redirecting the player's focus.

## File List

### Frontend — Modified Files
- `bingo-frontend/src/components/player/GameRoom.vue` [MODIFIED — added near-win indicators, mini-map flashes, slide-in banner, and corresponding styles/transitions]

## Dev Agent Record

### Agent Model Used
Gemini 3.5 Flash (High) / Antigravity

### Validation Results
- **TypeScript Check:** Passed with no errors.
