# Story 7.4: Admin Lifecycle Management & Gameplay Constraint Guardrails

Status: done

## Story

As a SuperAdmin,
I want to deactivate, delete, or generate password reset codes for administrator accounts and enforce strict state restrictions on rounds,
so that unauthorized admin activity is prevented and game flow rules are programmatically enforced.

## Acceptance Criteria

1. **Given** an administrator account, **When** toggled or deleted by a SuperAdmin, **Then** the backend must block self-deletion/deactivation, invalidate active sessions of the targeted admin, and restrict endpoints to `ROLE_SUPER_ADMIN`.
2. **Given** the event game flow, **When** no round is active, **Then** drawing actions (clicks/hotkeys) must be disabled and display a prominent warning banner.
3. **Given** a round status change, **When** starting a round, **Then** the system must block starting it if the parent event status is `AGUARDANDO` or not `ATIVO`.

## Tasks / Subtasks

- [x] **Task 1: Database & Backend (Admins)**
  - [x] Create Flyway migration `V13__add_active_column_to_admins.sql` to add the `active` column.
  - [x] Create admin management endpoints in `AdminManagementController.java`.
  - [x] Protect endpoints under `SecurityConfig.java` and invalidate admin sessions inside `AdminSessionRepository.java`.
- [x] **Task 2: Backend (Gameplay Safeguards)**
  - [x] Restrict `recordDraw` in `GameService.java` to fail if no round is in-progress.
  - [x] Restrict round starting in `RoundController.java` to fail if parent event is `AGUARDANDO`.
- [x] **Task 3: Frontend (Admin management & Controls)**
  - [x] Create the administrator list and action dialog in `AdminDashboard.vue`.
  - [x] Disable drawing board and hotkeys in `EventGameConsole.vue` when no round is active; add red banner warning.
  - [x] Block starting rounds in frontend if event state is not `ATIVO`.
  - [x] Clean up `EventCreator.vue` by removing redundant pattern fields.

## Dev Notes

- Admin inactivation is instantly propagated because `AdminAuthenticationFilter` performs database lookups on the `active` column for each request.
- Self-deactivation and self-deletion are strictly validated on both frontend and backend.
