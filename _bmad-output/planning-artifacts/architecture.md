---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
inputDocuments:
  - prd.md
  - ux-design-specification.md
  - product-brief-bingo-ai.md
workflowType: 'architecture'
project_name: 'bingo-ai'
user_name: 'Jeyson'
date: '2026-05-22'
lastStep: 8
status: 'complete'
completedAt: '2026-05-24'
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**
- **Event & Ticket Lifecycle (FR1, FR2, FR3)**: Complete support for event creation, prize configuration, high-volume secure ticket generation (unique ID + QR code + PDF export), and zero-friction activation via QR scan.
- **Synchronized Active Gameplay (FR4, FR5)**: Simultaneous rendering of the YouTube live stream iframe and the interactive 5x5 grid ticket ("Cartelão"). Automatic marking of drawn numbers synced with broadcast delay.
- **Admin Control & Moderation (FR6, FR8)**: An administrative control grid for drawing numbers, a latency synchronizer (0-30s), live data overrides, and a broadcast mechanism.
- **Secure Verification Loop (FR7)**: Server-side validation of win claims ("BINGO!") based on state history, with audit dashboards for visual confirmation.

**Non-Functional Requirements:**
- **Real-Time Performance**: Low-latency WebSocket messages reaching 95% of clients within 500ms. Centralized client event queue to defer render updates matching the stream lag.
- **Security & Integrity**: 100% server-side verification for all victory claims to prevent client-side tampered claims. Encryption at rest and in transit.
- **High Accessibility (WCAG 2.1 AA)**: Minimum contrast ratio of 4.5:1, visual redundancy (check icon `✓` with green colors for colorblind players), large touch targets (48x48px), and screen reader landmark compliance with `aria-live` polite announcements.
- **Robustness**: 99.9% uptime during event windows, with real-time state persistence to recover from server crashes or network disconnections.

**Scale & Complexity:**
- **Primary Domain**: Full-Stack Web Application (Vue.js SPA + Spring Boot REST & WebSocket API).
- **Complexity Level**: Medium-High, driven by real-time WebSocket state management for 500+ concurrent users, dynamic client-side event delay queues, and accessibility guarantees.
- **Estimated Architectural Components**: ~5 backend modules (Event Service, Ticket Engine, Game/WebSocket Session Manager, Verification Engine, Audit Logger) and ~4 frontend components (`BingoCard`, `CardMiniMap`, `YouTubeSyncPlayer`, `AdminDrawBoard`).

### Technical Constraints & Dependencies
- **WebSocket Protocol (Java Spring Boot + Pinia)**: High-concurrency WebSocket server handling burst events (numbers drawn, win claims).
- **Latency Delay Wizard**: Client-side state queue to defer WS state visual marking updates, aligned with the YouTube Live RTMP/HLS delay.
- **No-Setup Authentication**: QR code URL embeds ticket ID; authorization is ticket-session based (no traditional signup/login for players).

### Cross-Cutting Concerns Identified
1. **Latency Alignment Engine**: Standardizing how the client queues and dispatches WebSocket state updates based on the current delay configuration.
2. **Accessible Keyboard & Screen Reader States**: Assuring focus states, ARIA-labels, and polite live-region alerts are synchronized correctly without interrupting audio.
3. **Safety & Audit Compliance**: Complete transaction tracing from Admin draw to server WebSocket broadcast, to player claim, to server validation, and Admin approval.

## Starter Template Evaluation

### Primary Technology Domain

**Full-Stack Hybrid Web Application** (Split-Architecture: Spring Boot REST & WebSocket Backend + Vue.js Single Page Application Frontend) based on project requirements analysis.

### Starter Options Considered

1. **Backend Starter: Spring Initializr (Maven + Java 21)**
   - **Details**: Standardized bootstrap tool for Spring Boot applications. We select Spring Boot `4.0.6` (latest stable GA as of May 2026) and Java `21` (LTS) to utilize virtual threads, robust garbage collection, and modern language features.
   - **Dependencies Included**: `web` (REST APIs), `websocket` (real-time bidirectionality), `data-jpa` (ORM), `postgresql` (database connection), `validation` (safety checks on Admin draws/claims), and `security` (for secure Admin login).
   - **Architectural Patterns**: Establishes standard Maven structure, application properties configurations, and clean separation between controller, service, repository, and configuration layers.

2. **Frontend Starter: Vuetify CLI (Vite + Vue 3 SPA)**
   - **Details**: Official Vuetify project creator (`npm create vuetify@latest`) using **Vite** as the ultra-fast build tool. It provides a standard Single Page Application layout, Vue Router for routing, and Pinia for game state.
   - **CSS Integration Strategy**: We reject pre-bundled styling solutions and combine Vuetify with **Tailwind CSS v4** via the `@tailwindcss/vite` plugin. To avoid style hierarchy conflicts, we use explicit CSS Cascade Layers to prioritize Vuetify components and override specific margins and spacing with Tailwind utilities safely.

### Selected Starter: bingo-ai Dual-Stack Architecture

**Rationale for Selection:**
- **Spring Boot 4.0.6 & Java 21**: Out-of-the-box support for virtual threads to handle 500+ concurrent WebSocket connections efficiently under low CPU usage. Spring Security protects Admin credentials safely.
- **Vue 3 + Vuetify + Tailwind v4**: Vuetify provides WCAG 2.1 AA compliant elements (like dialogs, forms, and cards) that reduce the development effort of building an accessible UI for elderly users. Vite ensures hot-reloading in milliseconds and low build sizes, while Tailwind offers precise control over animations and micro-interactions.

**Initialization Commands:**

For the Backend:
```bash
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d javaVersion=21 \
  -d bootVersion=4.0.6 \
  -d dependencies=web,websocket,data-jpa,postgresql,validation,security \
  -d baseDir=bingo-backend \
  -o bingo-backend.zip
```

For the Frontend:
```bash
npm create vuetify@latest bingo-frontend -- --preset base
```

**Architectural Decisions Provided by Starters:**

**Language & Runtime:**
- **Backend**: Java 21 (LTS) runtime using standard JVM packaging.
- **Frontend**: TypeScript (`strict` mode enabled by default) running on modern ESNext browsers.

**Styling Solution:**
- Tailwind CSS v4 integrated directly via `@tailwindcss/vite`.
- Custom CSS cascade layers to prevent styling collisions between Material Design rules and Tailwind resets:
  `@layer tailwind-theme, tailwind-reset, vuetify-core, vuetify-components, vuetify-overrides, vuetify-utilities, tailwind-utilities;`

**Build Tooling:**
- **Backend**: Maven (`pom.xml`) for dependency management and clean packaging.
- **Frontend**: Vite (`vite.config.mts`) with native ES Module bundling, enabling extremely fast build speeds and automatic asset caching.

**Testing Framework:**
- **Backend**: JUnit 5 + Spring Boot Starter Test for unit and integration testing.
- **Frontend**: Vitest for lightning-fast unit tests and Cypress or Playwright for End-to-End browser validation.

**Code Organization:**
- **Backend**: Standard package-by-feature layout (`com.bingoai.backend.event`, `com.bingoai.backend.ticket`, etc.).
- **Frontend**: Single File Components (`.vue` SFC) divided into logical atomic parts (`src/components/game/`, `src/components/admin/`) and a centralized reactive Pinia store (`src/stores/game.ts`).

**Development Experience:**
- Vite Hot Module Replacement (HMR) for visual editing.
- Centralized Environment configurations (`application.yml` and `.env`).
- Spring Boot DevTools for backend auto-reload on recompile.

**Note:** Project initialization using these commands will be the first implementation story of our sprint plan.

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**
- **Server-Side Win Engine**: Win validation is executed strictly on the backend to avoid client-side tampering, securing the physical-digital bridge's integrity.
- **Latency Delay Queues**: Standardized client-side buffer inside Pinia to queue WebSocket events and execute visual triggers in synchronization with YouTube stream delay (0-30s).
- **No-Credential Authentication**: Ticket activation based on UUID inside the scanned QR code URL, generating short-lived memory session tokens rather than requiring user registrations.

**Important Decisions (Shape Architecture):**
- **WebSocket STOMP Integration**: Bidirectional real-time gameplay utilizing `@stomp/stompjs` (v7.3.0) for drawn ball broadcasts and win claims.
- **Flyway Database Migrations**: Relational schema version control managed by Flyway (v10+), assuring structural safety over entity auto-generation.

**Deferred Decisions (Post-MVP):**
- **Redis Distributed Cache**: Deferred until Phase 2/3. Caffeine In-Memory cache within the JVM is sufficient for the MVP's 500+ simultaneous regional users.
- **Moderated Player Chat**: Deferred to Phase 2 to minimize initial WebSocket event traffic overhead.

### Data Architecture

- **Database Choice**: PostgreSQL (v16+) for relational, ACID-compliant persistence of rounds, tickets, drawn balls, and verification logs.
- **Migration Approach**: Schema changes controlled exclusively by Flyway SQL migration scripts to enforce reproducible states.
- **JPA & ORM**: Hibernate/Spring Data JPA mapping relational event contexts.
- **Caching Strategy**: Local JVM cache using Caffeine to quickly retrieve configuration constants and active ticket layouts.

### Authentication & Security

- **Player Authentication**: Ticket-session based authorization. Scan of the physical ticket QR code triggers a secure ticket validation endpoint, returning a temporary JWT stored in memory for WebSocket handshakes.
- **Admin Authentication**: Spring Security form-based login using secured HttpOnly session cookies to mitigate CSRF/XSS vectors.
- **Win Integrity Protection**: Server audits ticket state by cross-referencing drawn balls from the db against the ticket's generated board matrix.

### API & Communication Patterns

- **API Styles**: RESTful JSON endpoints for administrative setups, combined with WebSocket over STOMP for lightweight real-time event broadcasts.
- **Real-Time Latency Engine**:
  - Backend broadcasts drawn balls immediately via WS.
  - Client stores the event, calculating absolute delay matching the Admin configured latency, executing visual card marks exactly when the video announces the number.

### Frontend Architecture

- **Centralized State**: Pinia store managing the reactive engine: `drawnNumbers`, `activeTickets`, `latencySeconds`, and `gameStatus` (Connected, Interrupted, BingoClaimed).
- **CSS Cascade Layers**: Explicit styling hierarchy separating Vuetify Material Design styles from Tailwind CSS resets to avoid component conflicts.
- **A11y ARIA Alerts**: Live announcements region styled in standard template using `aria-live="polite"` triggered reactively via Pinia store changes.

### Infrastructure & Deployment

- **Containerization**: Standard Docker Compose configuration with multi-stage `Dockerfile` layers for Spring Boot and Nginx (Vue SPA static server).
- **Hosting Strategy**:
  - Frontend: High-performance CDN (Vercel/Netlify).
  - Backend: Managed cloud containers (Railway/AWS App Runner) with PostgreSQL managed services (RDS).

### Decision Impact Analysis

**Implementation Sequence:**
1. Database Schema migrations configuration (Flyway).
2. Backend REST Endpoints (Event creation & PDF Ticket Generation).
3. Centralized Pinia state store & UI Layout skeleton.
4. WebSocket STOMP integration (Drawn numbers broadcast).
5. Frontend Client-side latency queuing logic implementation.
6. Server-side Win audit engine completion.

**Cross-Component Dependencies:**
- The **Win Engine** relies strictly on Flyway DB ticket models, while the **Latency Engine** requires custom WebSocket state synchrony mapped reactively in the Pinia store.

## Implementation Patterns & Consistency Rules

### Pattern Categories Defined

**Critical Conflict Points Identified:**
5 key areas where AI agents could make different choices if left unspecified:
1. **Database Schema Naming & Conventions** (e.g., singular vs plural tables, casing).
2. **API Endpoint and Payload Conventions** (e.g., camelCase vs snake_case).
3. **Frontend Component & Routing Structures** (e.g., multi-word PascalCase, file layout).
4. **WebSocket/STOMP Event payload shapes & topics** (e.g., topic prefixing, standardized status fields).
5. **Global Error Handling and Loading State Structures** (e.g., loading spinners, standardized HTTP error bodies).

### Naming Patterns

**Database Naming Conventions:**
- **Table Names:** Plural snake_case (`events`, `tickets`, `drawn_numbers`, `verification_logs`).
- **Column Names:** Lowercase snake_case (`event_id`, `created_at`, `ticket_matrix`).
- **Primary Keys:** Standard column name `id` of type UUID or BIGINT.
- **Foreign Keys:** Pattern `[parent_table_singular]_id` (e.g., `event_id` in the `tickets` table).
- **Index Names:** `idx_[table_name]_[column_names_joined_by_underscore]` (e.g., `idx_tickets_uuid`).

**API Naming Conventions:**
- **REST Endpoints:** Plural kebab-case prefixed with `/api/v1/` (e.g., `/api/v1/events`, `/api/v1/tickets/{id}/activate`).
- **Route Parameters:** Lowercase UUID strings `{id}` in endpoints.
- **Query Parameters:** Lowercase camelCase (e.g., `?page=1&size=20`).
- **Request/Response JSON Payloads:** camelCase (e.g., `eventId`, `ticketUuid`, `drawnBall`).

**Code Naming Conventions:**
- **Frontend Components:** PascalCase with multi-word names (e.g., `BingoCard.vue`, `YouTubeSyncPlayer.vue`, `AdminDrawBoard.vue`).
- **Frontend State (Pinia):** camelCase for files (`useGameStore.ts`) and functions/variables.
- **Backend Class Names:** Standard Java PascalCase with feature postfixes (e.g., `EventController.java`, `TicketService.java`, `DrawnNumberRepository.java`).
- **Methods and Variables:** camelCase (e.g., `drawNumber()`, `activeTickets`).

### Structure Patterns

**Project Organization:**
- **Backend Layout:** Package-by-feature pattern:
  - `com.bingoai.backend.event` (Controllers, Services, Repositories, Entities)
  - `com.bingoai.backend.ticket` (Controllers, Services, Repositories, Entities)
  - `com.bingoai.backend.game` (WebSocket controllers, Session state managers)
- **Frontend Layout:** Component grouping by module:
  - `src/components/game/` (Player components like `BingoCard.vue`)
  - `src/components/admin/` (Moderator controls like `AdminDrawBoard.vue`)
  - `src/components/shared/` (Layout grids, universal buttons, accessibility alerts)
  - `src/stores/` (Pinia state files: `game.ts`, `admin.ts`)
- **Tests Location:**
  - Backend: Standard Maven `src/test/java/...` mimicking the source package layout.
  - Frontend: Co-located `*.spec.ts` files inside the same folder as their corresponding components or stores.

**File Structure Patterns:**
- **Environment Variables:** Configured in `application.yml` for Spring Boot, and `.env` / `.env.production` in Vue 3 root.
- **Static Assets:** Static resources like sound effects, SVG icons, and fallback layouts placed inside `src/assets/` on frontend.

### Format Patterns

**API Response Formats:**
- **Standard REST Wrapper:** All endpoints return a uniform envelope:
  ```json
  {
    "success": true,
    "data": { ... },
    "error": null
  }
  ```
- **Standard REST Error Envelope:**
  ```json
  {
    "success": false,
    "data": null,
    "error": {
      "code": "VALIDATION_FAILED",
      "message": "The selected ticket has already been activated.",
      "details": ["Field 'ticketUuid' must be a valid UUID format."]
    }
  }
  ```
- **Date Formats:** Always serialized as ISO-8601 strings in UTC (e.g., `2026-05-24T18:38:30Z`).
- **Boolean Representations:** Strict JavaScript booleans (`true`/`false`).

**Data Exchange Formats:**
- JSON serialization in Jackson (backend) and standard Pinia stores (frontend) must enforce camelCase and discard null fields to minimize payload size during high-volume sessions.

### Communication Patterns

**Event System Patterns:**
- **WebSocket Topics:**
  - `/topic/events/{eventId}/draws` (Broadcast of newly drawn numbers)
  - `/topic/events/{eventId}/claims` (Broadcast of verified BINGOs to all players)
  - `/app/events/{eventId}/claim` (Inbound destination for ticket win submissions)
- **WebSocket Message Schema:** Standard envelope containing metadata:
  ```json
  {
    "eventType": "BALL_DRAWN",
    "timestamp": "2026-05-24T18:38:30Z",
    "payload": {
      "ballNumber": 42,
      "drawnSequence": 15
    }
  }
  ```

**State Management Patterns:**
- **Pinia State Updates:** State changes must be completed inside explicit actions within `useGameStore.ts`. Direct mutations outside actions are strictly prohibited.
- **Action Naming:** Verb-first pattern (e.g., `updateDrawnNumbers()`, `setConnectionStatus()`, `triggerBingoClaim()`).

### Process Patterns

**Error Handling Patterns:**
- **Backend Exception Handler:** A global `@RestControllerAdvice` translates Spring exceptions into standard JSON error wrappers.
- **Frontend Error Handler:** Standard Vue global error boundaries catch render issues, whereas API/WebSocket failures are routed to a toast notification store (`useNotificationStore`).
- **User-Facing Message Standards:** Messages must be accessible and distinct, avoiding technical jargon (e.g., "Connection lost. Reconnecting automatically..." instead of "WebSocket connection closed with code 1006").

**Loading State Patterns:**
- **Store-Level States:** Pinia stores track loading states using boolean properties (e.g., `isLoading`, `isSubmittingClaim`).
- **Visual Feedback:** All network actions must trigger appropriate skeleton loaders or disabled button states (with explicit `aria-busy="true"` tags).

### Enforcement Guidelines

**All AI Agents MUST:**
1. Follow the standard camelCase JSON response casing.
2. Utilize explicit `@layer` ordering to prevent Vuetify and Tailwind styling overrides from breaking the responsive layout.
3. Never trust client-side validation for winning claims; all claims must be verified server-side.

**Pattern Enforcement:**
- **Continuous Checking:** Validate code syntax, types, and naming with ESLint + Prettier (frontend) and SpotBugs/Checkstyle (backend).
- **Updating Patterns:** When pattern changes are required, developers must update this architectural document before modifying source layouts.

### Pattern Examples

**Good Examples:**
- *Database Migrations:* A clean Flyway file `V1__create_events_table.sql`:
  ```sql
  CREATE TABLE events (
      id UUID PRIMARY KEY,
      title VARCHAR(255) NOT NULL,
      created_at TIMESTAMP WITH TIME ZONE NOT NULL
  );
  ```
- *REST Controller Method:*
  ```java
  @GetMapping("/api/v1/events")
  public ResponseEntity<ApiResponse<List<EventDto>>> getActiveEvents() { ... }
  ```

**Anti-Patterns:**
- *Avoid direct entities in endpoints:* Do not expose database models directly. Always use DTOs to avoid leaking structural properties or deserialization exploits.
- *Avoid direct state manipulation:* Do not write `store.drawnNumbers.push(num)` directly from components. Always call a store action: `store.addDrawnNumber(num)`.
- *Avoid mixing styles:* Do not override Vuetify component styles using inline styles. Utilize tailwind-utilities or layer-configured CSS overrides.

## Project Structure & Boundaries

### Complete Project Directory Structure

```
bingo-ai/
├── README.md
├── docker-compose.yml
├── .gitignore
├── bingo-backend/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── bingoai/
│   │   │   │           └── backend/
│   │   │   │               ├── BingoBackendApplication.java
│   │   │   │               ├── common/
│   │   │   │               │   ├── config/
│   │   │   │               │   │   ├── SecurityConfig.java
│   │   │   │               │   │   ├── WebSocketConfig.java
│   │   │   │               │   │   └── CacheConfig.java
│   │   │   │               │   ├── exception/
│   │   │   │               │   │   ├── GlobalExceptionHandler.java
│   │   │   │               │   │   └── CustomException.java
│   │   │   │               │   └── dto/
│   │   │   │               │       └── ApiResponse.java
│   │   │   │               ├── event/
│   │   │   │               │   ├── EventController.java
│   │   │   │               │   ├── EventService.java
│   │   │   │               │   ├── EventRepository.java
│   │   │   │               │   └── Event.java
│   │   │   │               ├── ticket/
│   │   │   │               │   ├── TicketController.java
│   │   │   │               │   ├── TicketService.java
│   │   │   │               │   ├── TicketRepository.java
│   │   │   │               │   ├── Ticket.java
│   │   │   │               │   └── util/
│   │   │   │               │       └── PdfGenerator.java
│   │   │   │               └── game/
│   │   │   │                   ├── GameWebSocketController.java
│   │   │   │                   ├── GameSessionManager.java
│   │   │   │                   ├── WinVerificationEngine.java
│   │   │   │                   └── dto/
│   │   │   │                       ├── DrawBallMessage.java
│   │   │   │                       └── BingoClaimMessage.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/
│   │   │           └── migration/
│   │   │               └── V1__init_schema.sql
│   │   └── test/
│   │       └── java/
│   │           └── com/
│   │               └── bingoai/
│   │                   └── backend/
│   │                       ├── event/
│   │                       │   └── EventServiceTests.java
│   │                       ├── ticket/
│   │                       │   └── TicketServiceTests.java
│   │                       └── game/
│   │                           └── WinVerificationTests.java
├── bingo-frontend/
│   ├── package.json
│   ├── vite.config.mts
│   ├── tsconfig.json
│   ├── .env
│   ├── .env.production
│   ├── index.html
│   ├── src/
│   │   ├── main.ts
│   │   ├── App.vue
│   │   ├── router/
│   │   │   └── index.ts
│   │   ├── plugins/
│   │   │   └── vuetify.ts
│   │   ├── styles/
│   │   │   └── main.css
│   │   ├── assets/
│   │   │   ├── logo.svg
│   │   │   └── sounds/
│   │   │       ├── beep.mp3
│   │   │       └── victory.mp3
│   │   ├── stores/
│   │   │   ├── game.ts
│   │   │   ├── admin.ts
│   │   │   └── notification.ts
│   │   ├── services/
│   │   │   ├── api.ts
│   │   │   └── websocket.ts
│   │   └── components/
│   │       ├── game/
│   │       │   ├── BingoCard.vue
│   │       │   ├── CardMiniMap.vue
│   │       │   ├── YouTubeSyncPlayer.vue
│   │       │   └── VictoryBanner.vue
│   │       ├── admin/
│   │       │   ├── AdminDrawBoard.vue
│   │       │   ├── LatencyDelayWizard.vue
│   │       │   └── AuditVerificationPanel.vue
│   │       └── shared/
│   │           ├── BaseButton.vue
│   │           ├── AccessibilityAnnouncer.vue
│   │           └── ToastNotification.vue
│   └── tests/
│       ├── unit/
│       │   ├── useGameStore.spec.ts
│       │   └── BingoCard.spec.ts
│       └── e2e/
│           └── gameplay.cy.ts
```

### Architectural Boundaries

**API Boundaries:**
- **REST Endpoints:** Expose standard administrative setup functions, public events feed, and ticket activation loops over secure HTTPS JSON APIs prefixed with `/api/v1/`. CORS configurations are strictly enabled to authorize only the frontend CDN domain.
- **WebSocket STOMP Broker:** Operates over standard `/ws/bingo` handshake endpoint, enforcing authentication before mapping client sessions to event queues.

**Component Boundaries:**
- Vue Single File Components (`SFC`) communicate state changes strictly through centralized stores inside `src/stores/`. Direct component-to-component parameter drilling is prohibited for game coordination.
- UI elements do not fetch data directly; they dispatch actions to the store which communicates with backend services.

**Service Boundaries:**
- **Backend Controller Layer:** Parses HTTP payloads, applies `@Valid` checks, and routes parameters.
- **Backend Service Layer:** Holds business logic (e.g., ticket generation matrices, session calculations) inside transactions.
- **Backend Repository Layer:** Abstract database actions. No SQL query leak is permitted above the service layer.

**Data Boundaries:**
- Relational schema definitions are encapsulated strictly inside Flyway SQL migrations. No entities may automatically alter tables in production settings.

### Requirements to Structure Mapping

**Feature/Epic Mapping:**
- **Event & Ticket Lifecycle (FR1, FR2, FR3):**
  - **Backend:** `com.bingoai.backend.event` and `com.bingoai.backend.ticket` folders. PDF generation logic lives in `PdfGenerator.java`.
  - **Frontend:** REST interactions managed in `api.ts`, rendering layout templates via Vuetify grids.
- **Synchronized Active Gameplay (FR4, FR5):**
  - **Backend:** WebSocket message routing in `GameWebSocketController.java` to broadcast ball numbers.
  - **Frontend:** Central coordination inside `useGameStore.ts`, displaying the 5x5 card via `BingoCard.vue` and the live video inside `YouTubeSyncPlayer.vue`.
- **Admin Control & Latency Config (FR6, FR8):**
  - **Backend:** Delay configurations handled in `GameSessionManager.java` state maps.
  - **Frontend:** Controls rendered in `AdminDrawBoard.vue` and customized lag settings in `LatencyDelayWizard.vue`.
- **Secure Verification (FR7):**
  - **Backend:** Validation routines inside `WinVerificationEngine.java` to test claim integrity.
  - **Frontend:** Results displayed to staff via `AuditVerificationPanel.vue`.

**Cross-Cutting Concerns:**
- **Accessibility & ARIA Alerts:** Standard alerts mapped within `AccessibilityAnnouncer.vue` utilizing localized `aria-live="polite"` broadcasts.
- **Temporary Authorization:** Configured inside `SecurityConfig.java` to permit password-free ticket handshakes using UUID configurations.

### Integration Points

**Internal Communication:**
- **REST Clients:** Frontend consumes HTTP REST APIs using Axios configured in `api.ts`.
- **WebSocket STOMP Channel:** Real-time bi-directional messaging handled by `@stomp/stompjs` encapsulated in `services/websocket.ts`.

**External Integrations:**
- **YouTube Live Stream Integration:** Client loads the YouTube IFrame Player API within `YouTubeSyncPlayer.vue` to dynamically pull video state, handle latency checks, and mute/unmute matching UX specifications.

**Data Flow:**
1. Moderator draws a number via `AdminDrawBoard.vue` → REST API POST to `/api/v1/events/{id}/draw`.
2. Backend saves number to DB → Broadcasts it instantly over STOMP topic `/topic/events/{id}/draws`.
3. Client WebSocket receiver intercepts the message → Pushes `ballNumber` and `timestamp` into the queue inside `useGameStore.ts`.
4. Client scheduler compares drawing timestamp against current system time and `latencySeconds` config.
5. Once target visual play delay is satisfied → The card highlights the corresponding grid number visually, plays standard alert sound, and announces the ball to the screen reader.

### File Organization Patterns

**Configuration Files:**
- Backend configuration is modularized in `application.yml` profiles.
- Frontend compilation settings, Tailwind CSS integration layers, and plugins are defined inside root-level Vite and TypeScript config files.

**Source Organization:**
- Backend source code utilizes strict package-by-feature layout to avoid spaghetti wiring.
- Frontend layers are separated into reactive store controllers, communication services, and presentational SFC files.

**Test Organization:**
- Unit tests live next to backend modules or store classes, while end-to-end user journeys are placed inside high-level integration suites.

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:**
- **Technology Stack Synergy:** Spring Boot 4.0.6 (Java 21 LTS) and Vue 3 (Vuetify + Tailwind v4 via Vite) compile, package, and deploy independently without version conflicts. Java 21 Virtual Threads natively support high-concurrency WebSocket STOMP connections at minimal CPU costs, aligning with client-side Pinia stores.
- **No Contradictory Choices:** The session-based temporary token validation structure completely replaces custom sign-up pipelines, preventing standard auth flow bloat.

**Pattern Consistency:**
- **Unified Casing:** Enforced camelCase on JSON exchanges maps natively to TypeScript classes on frontend, while backend repositories serialize from lowercase snake_case PostgreSQL schemas via Hibernate properties translation.
- **Global Error Formats:** Unified API REST envelope structure wraps exceptions identically, eliminating parsing errors across components.

**Structure Alignment:**
- **Boundary Separation:** The split `bingo-backend` (Maven) and `bingo-frontend` (Vite) directory trees physically isolate layers, ensuring that server business logic and client view actions remain decoupled.

### Requirements Coverage Validation ✅

**Epic/Feature Coverage:**
- **Event Management & Verification Logs:** Supported via standard Flyway migration structures and backend JPA repository mapping.
- **Physical-to-Digital Bridge:** Supported by backend automated PDF generation using `PdfGenerator.java` mapping unique ticket UUID vectors.

**Functional Requirements Coverage:**
- **FR1, FR2, FR3 (Lifecycle & PDF):** Fully mapped to backend features and REST endpoints.
- **FR4, FR5 (Active Gameplay Synchronization):** Directly handled by `/topic/events/{id}/draws` broadcast paths and local Pinia delay managers.
- **FR6, FR8 (Moderator Panel & Latency):** Addressed via local `useAdminStore` forms sending latency constants.
- **FR7 (Secure Auditing):** Covered by backend `WinVerificationEngine` performing exact audits of matrix intersections against active database states.

**Non-Functional Requirements Coverage:**
- **Real-Time Latency (NFR1):** Managed via Pinia state buffers delaying visual render loops matching broadcast stream lag.
- **A11y Accessibility (NFR3):** Addressed by Vuetify AA components combined with explicit `AccessibilityAnnouncer.vue` alerting screen readers on new ball draws.
- **Security & Integrity (NFR2):** 100% server-side validation rules prevent client matrix hacking.

### Implementation Readiness Validation ✅

**Decision Completeness:**
- All critical technology components are locked with verified versions (Spring Boot 4.0.6, STOMPJS v7.3.0, PostgreSQL 16).
- Pattern templates (REST APIs, WS message payload structures, CSS layer ordering) are fully documented with concrete implementation details.

**Structure Completeness:**
- The directory trees are fully specified down to individual class, component, and utility files.

**Pattern Completeness:**
- Consistency guidelines define exactly how agents must behave regarding CSS conflicts, naming, state mutations, and error handling toasts.

### Gap Analysis Results

- **Critical Gaps:** None. All core gameplay features, authentication methods, synchronization engines, and security frameworks are fully defined.
- **Important Gaps:** None. Core naming schemas, API envelopes, and directories are locked.
- **Nice-to-Have Gaps:**
  - Standardized CI/CD build scripts (GitHub Actions config) are deferred to the active sprint planning stage.
  - Secondary fallback CDN routing and Redis caches are deferred to Post-MVP scaling phases (1000+ active players).

### Validation Issues Addressed

- **Styling Collisions:** Resolved styling hierarchy conflicts between Material Design and Tailwind CSS resets by introducing custom CSS Cascade Layers in `@layer vuetify-overrides` and `@layer tailwind-utilities`.
- **Latency Deferrals:** Designed a client-side Pinia event queue buffer instead of trying to synchronize WebSockets at network levels, ensuring frame correctness even under volatile cell signals.

### Architecture Completeness Checklist

**Requirements Analysis**
- [x] Project context thoroughly analyzed
- [x] Scale and complexity assessed
- [x] Technical constraints identified
- [x] Cross-cutting concerns mapped

**Architectural Decisions**
- [x] Critical decisions documented with versions
- [x] Technology stack fully specified
- [x] Integration patterns defined
- [x] Performance considerations addressed

**Implementation Patterns**
- [x] Naming conventions established
- [x] Structure patterns defined
- [x] Communication patterns specified
- [x] Process patterns documented

**Project Structure**
- [x] Complete directory structure defined
- [x] Component boundaries established
- [x] Integration points mapped
- [x] Requirements to structure mapping complete

### Architecture Readiness Assessment

- **Overall Status:** READY FOR IMPLEMENTATION
- **Confidence Level:** high
- **Key Strengths:**
  - High-performing Virtual Threads architecture enabling 500+ lightweight local WS sessions.
  - Robust double-layer win validation protecting game cash prizes.
  - Zero-barrier user activation via fast QR Code scanning.
- **Areas for Future Enhancement:**
  - Integrating automated integration tests with Playwright to validate real-time delay states under mock delays.

### Implementation Handoff

**AI Agent Guidelines:**
- Follow all architectural decisions exactly as documented.
- Use implementation patterns consistently across all components.
- Respect project structure and boundaries.
- Refer to this document for all architectural questions.

**First Implementation Priority:**
Initialize the dual-stack folders using the commands:
- **Backend:** `curl https://start.spring.io/starter.zip -d type=maven-project -d javaVersion=21 -d bootVersion=4.0.6 -d dependencies=web,websocket,data-jpa,postgresql,validation,security -d baseDir=bingo-backend -o bingo-backend.zip`
- **Frontend:** `npm create vuetify@latest bingo-frontend -- --preset base`
