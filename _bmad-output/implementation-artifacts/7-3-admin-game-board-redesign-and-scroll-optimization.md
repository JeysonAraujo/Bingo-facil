# Story 7.3: Admin Game Board Redesign & Scroll Optimization

Status: done

## Story

As an Event Coordinator,
I want the drawing board numbers to be arranged in sequential vertical columns and the event audit panels to be grouped cleanly below,
so that I can control draws quickly and verify claims without scanning or scrolling the page continuously.

## Acceptance Criteria

1. **Given** the 75-key admin board, **When** rendered, **Then** numbers must progress vertically within sub-columns of 5 under their respective letter headers (B: 1-5, 6-10...; I: 16-20...).
2. **Given** the Claims and Round History sections, **When** rendered on the event console, **Then** they must sit side-by-side underneath the drawing board with internal scrolling rather than expanding the entire page height.

## Tasks / Subtasks

- [x] **Task 1: Frontend (Redesign)**
  - [x] Implement the `orderedBalls` computed property in `EventGameConsole.vue` to compute the vertical sequencing of the B-I-N-G-O columns.
  - [x] Group the **Bingo Claims** and **Round History** cards side-by-side using Vuetify layout columns.
  - [x] Add inline CSS class constraints (`max-height: 250px`, `overflow-y: auto`) to both cards for clean scrollability.

## Dev Notes

- Replaces the linear 1-75 array indexing with vertical sub-column mapping.
- Greatly increases usability by putting all game control functions above the fold.
