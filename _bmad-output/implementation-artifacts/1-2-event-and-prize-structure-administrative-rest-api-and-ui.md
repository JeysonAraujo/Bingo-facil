# Story 1.2: Event & Prize Structure Administrative REST API & UI

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As an Event Coordinator,
I want to create an event and specify custom round prizes (e.g., Column, Line, Full Card) in a dashboard,
so that I can customize event metadata and gameplay rules.

## Acceptance Criteria

1. **Given** an initialized backend running on PostgreSQL
2. **When** I submit a POST request to `/api/v1/events` containing name, scheduled time, and prize structures
3. **Then** the event is successfully persisted in the database and returned in standard REST ApiResponse envelope
4. **And** the Admin web view displays the created event details in a high-contrast Material design dashboard.

## Tasks / Subtasks

- [x] **Task 1: Backend Setup & DTOs** (AC: 2, 3)
  - [x] Create standardized `ApiResponse` and `ApiError` envelopes.
  - [x] Create DTOs (`CreateEventRequest`, `CreateRoundRequest`, `CreatePrizeRequest`, etc.).
- [x] **Task 2: Database Entities & Repositories** (AC: 1, 3)
  - [x] Create JPA entities: `Event`, `Round`, `Prize` with appropriate mappings.
  - [x] Create `EventRepository`.
- [x] **Task 3: Service & Controller** (AC: 2, 3)
  - [x] Implement `EventService` with transactional creation of events and nested components.
  - [x] Implement `EventController` exposing `/api/v1/events`.
  - [x] Add `GlobalExceptionHandler` to format errors in the standard envelope.
- [x] **Task 4: Frontend API Integration & State** (AC: 4)
  - [x] Configure Axios client (`api.ts`).
  - [x] Create Pinia store (`admin.ts`) for managing events state.
  - [x] Map frontend routing for administrative views.
- [x] **Task 5: UI Implementation** (AC: 4)
  - [x] Build `AdminDashboard.vue` to list events.
  - [x] Build `EventCreator.vue` to configure new events and prizes.

## Dev Notes

- O desenvolvimento original e a documentação dessa Story foram registrados no arquivo `docs/Implementing Story 1.2 Administrative API and UI.md` devido a um erro de direcionamento de pasta na execução do BMAD.
- Este artefato foi reconstruído e formatado com o template BMAD para manter a consistência e marcar formalmente a conclusão da história.
