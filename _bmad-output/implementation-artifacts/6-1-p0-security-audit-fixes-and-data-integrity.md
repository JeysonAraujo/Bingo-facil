# Story 6.1: P0 Security Audit Fixes and Data Integrity

Status: done

## Story

As an Event Coordinator and Player,
I want the platform to be secure and enforce strict data integrity rules,
so that unauthorized access is prevented, game state remains consistent, and player data is safely handled.

## Acceptance Criteria

1. **Given** a generated ticket, **When** a player accesses it, **Then** they must provide Name and Phone number, and the session token must be properly rotated upon re-authentication.
2. **Given** a finished round or event, **When** an admin tries to change its status or modify it, **Then** the system must block the action to maintain immutability.
3. **Given** public game events (WebSocket), **When** a claim is approved, **Then** the player's name is broadcasted but the sensitive session token must never be exposed.
4. **Given** event header images, **When** uploaded or deleted, **Then** the file system must store them securely using relative paths and delete old files properly.
5. **Given** the database schema, **When** the application starts, **Then** it must apply strict domain constraints, uniqueness rules, and indexing for optimized session cleanup.

## Tasks / Subtasks

- [x] **Task 1: Database (Migrations)**
  - [x] Create migration `V11__add_player_details_and_security_constraints.sql` with player columns and domain constraints.
- [x] **Task 2: Backend (Entities & DTOs)**
  - [x] Update `Ticket.java` and `ActivateTicketRequest.java` with `playerName` and `playerPhone`.
- [x] **Task 3: Backend (Services)**
  - [x] Implement secure activation and login with session token rotation in `TicketActivationService.java`.
  - [x] Add ownership and status validations in `TicketService.java` and `EventService.java`.
  - [x] Ensure relative paths for header image uploads and physical file deletion.
- [x] **Task 4: Backend (Controllers & Security)**
  - [x] Add ownership validations across `TicketController`, `RoundController`, `AdminTicketController`, `ClaimController`, and `GameController`.
  - [x] Prevent round status changes on finished rounds.
  - [x] Fix unintended auto-start of rounds on `/draws` endpoint.
  - [x] Remove `sessionToken` from `BingoClaimMessage` broadcast payload.
  - [x] Secure header image endpoints in `EventController.java`.
- [x] **Task 5: Frontend (API, Stores & Components)**
  - [x] Update TypeScript definitions in `api.ts`.
  - [x] Update game store to handle multiple winners (ties) and persist player name.
  - [x] Modify `TicketActivation.vue` to require Name and Phone.
  - [x] Update `GameRoom.vue` to display the winner's name and handle ties.
  - [x] Add confirmation prompts for destructive actions in `EventGameConsole.vue` and `AdminDashboard.vue`.

## Dev Notes

- These changes were implemented to address the P0 vulnerabilities and architectural flaws identified in the security audit.
- State immutability for rounds and events guarantees that a finished game cannot be accidentally or maliciously rolled back or altered.
- Resolves issue C-03 (Session token reuse) and C-02 (Session token leakage on WebSocket).
