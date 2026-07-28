# Story 7.2: Custom Prize Type & Admin Control Endpoints Validation

Status: done

## Story

As an Event Coordinator,
I want the administrative round control interface to support advanced winning patterns (diagonal, outer border, inner border) without causing validation errors,
so that I can coordinate more diverse game dynamics for players.

## Acceptance Criteria

1. **Given** the round creation payload, **When** I choose custom winning patterns (Diagonal, Outer Border, Inner Border), **Then** the backend must accept and recognize these types.
2. **Given** the admin dashboard dropdown, **When** adding a new round, **Then** options must map directly to valid enum values on the backend instead of outdated values like 'PHYSICAL'.

## Tasks / Subtasks

- [x] **Task 1: Backend (Enums)**
  - [x] Add new entries `DIAGONAL`, `OUTER_BORDER`, and `INNER_BORDER` to `PrizeType.java`.
- [x] **Task 2: Frontend (Components)**
  - [x] Update the pattern selection options in `EventGameConsole.vue` to map directly to the backend's expected values.
  - [x] Clean up the default pattern value mapping logic in `submitRound` to ensure the correct payload translation.

## Dev Notes

- Fixes the `400 Bad Request` validation error when trying to start rounds with non-standard patterns.
