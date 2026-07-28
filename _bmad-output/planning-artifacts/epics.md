---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - prd.md
  - architecture.md
  - ux-design-specification.md
  - product-brief-bingo-ai.md
---

# bingo-ai - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for bingo-ai, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Admin can create events and manage prize structures for each round.
FR2: Admin can generate unique ticket IDs, QR codes, and printable PDF batches.
FR3: Users can activate tickets via QR scan or ID entry.
FR4: Users can view a synchronized YouTube Live stream and digital "Cartelão" simultaneously.
FR5: System automatically marks tickets and triggers "near-win" and "victory" animations.
FR6: Admin can manually record drawn numbers and adjust stream synchronization delay.
FR7: System verifies "Bingo!" claims on the server and provides visual proof for Admin confirmation.
FR8: Admin can broadcast messages and override ticket data in real-time.

### NonFunctional Requirements

NFR1: Latency: WebSocket messages must reach 95% of clients within 500ms.
NFR2: Security: All player data encrypted at rest and in transit; server-side-only verification for all wins.
NFR3: Accessibility: High-contrast UI (4.5:1 ratio) and large touch targets (minimum 48x48px with 8px margins) for elderly users.
NFR4: Reliability: 99.9% uptime during event windows; real-time state persistence for crash recovery.

### Additional Requirements

- Dual-Stack split architecture (Spring Boot 4.0.6 backend and Vue 3 + Vuetify + Tailwind CSS v4 frontend packaged via Vite).
- Virtual Threads enabled on Spring Boot to scale 500+ simultaneous WebSocket sessions.
- Relational PostgreSQL (v16+) database with Flyway database migrations and local Caffeine JVM caching.
- Ticket-Session based authorization via ticket UUID inside QR code (no standard registration/passwords for players).
- Client-Side Latency Buffer mapping STOMP event delay with Admin set delay seconds (0-30s).
- Double-click/double-tap grid validation in Admin panel and undo operations.

### UX Design Requirements

UX-DR1: A 3-step interactive progressive disclosure highlight tutorial guiding players through UI landmarks (Live Stream, Auto-Marking Cartelão, and Bingo Claim Button).
UX-DR2: Card Mini-Map Tracker: Compact mobile layout mapping a fixed YouTube Sync Player, Card Mini-Map horizontal selector, and the active BingoCard Carousel, with "+1" badges and glowing near-win highlights.
UX-DR3: Responsive Game Split: Layout desktop in 2 columns (Live and Chat on the left, Cartelão in 2x2 grid on the right) and mobile in single column (Live fixed on top, Mini-map, Carousel with 1 card visible).
UX-DR4: Interactive Bingo Card Component: Reusable 5x5 grid with pre-marked "FREE" center, green pulse marked animations (#2E7D32), colorblind accessibility check (✓), and gold pulsing glowing border (#FFD600) in near-win state.
UX-DR5: Safe Admin Drawing Board: Double-tap / double-click 75-key grid safeguard on Admin panel, keyboard hotkeys (Enter to confirm, Esc to cancel, Ctrl+Z to undo), and side-by-side claim validation audit visual display.
UX-DR6: Victory Celebration: Audio chimes, screen haptics, full-screen canvas confetti explosion effects, and programmatic focus shift to the gold pulsing bingo claim button.

### FR Coverage Map

FR1: Epic 1 - Admin event creation and prize structure setup.
FR2: Epic 1 - Batch ticket generation, UUID/QR mapping, and PDF export.
FR3: Epic 2 - Ticket activation via QR scan link or manual code entry.
FR4: Epic 2 & Epic 4 - Live stream layout and real-time synchronized playback.
FR5: Epic 4 - Automatic marking reactivity, near-win alerts, and visual maps.
FR6: Epic 3 - Admin 75-key draw board, double-tap validation, and latency sync settings.
FR7: Epic 5 - Server-side win claim audit engine, admin conference panel, and celebrations.
FR8: Epic 3 - Real-time admin announcements, message broadcasting, and overrides.

## Epic List

### Epic 1: Event Initialization & Ticket Distribution Infrastructure
Set up basic project layout, database schema migrations, and administrative interfaces for event management and high-volume printable PDF ticket generation.
**FRs covered:** FR1, FR2

### Epic 2: Frictionless Ticket Activation & Player Game Lobby
Enable zero-friction, password-free player onboarding via physical ticket QR code scan or 6-digit code entry, leading to a clean, responsive game lobby.
**FRs covered:** FR3, FR4 (partial)

### Epic 3: Real-Time Sourced Admin Board & Broadcast Control
Build a robust real-time STOMP WebSocket broadcast manager and an error-proof 75-key administrative panel with input safety guards and live message broadcasting.
**FRs covered:** FR6, FR8

### Epic 4: Synchronized Passive Gameplay & Automated Marking Engine
Implement a client-side Pinia latency alignment queue that delays drawn number activations to perfectly synchronize with live YouTube stream lag, displaying near-win visual frames and mini-maps.
**FRs covered:** FR4 (synchronized stream), FR5

### Epic 5: Secure Server-Side Auditing & Victory Celebration
Establish a server-side win claims audit engine to prevent tampering, coupled with moderator approval views and immersive victory celebration overlays.
**FRs covered:** FR7

### Epic 6: Security, Stabilization & Quality Audit Fixes
Enforce strict security, data integrity, LGPD compliance, CI automation, and UX stabilization across backend database/filters and frontend screens.
**FRs covered:** Security Audit (C-02, C-03), LGPD Privacy, CI/CD Actions, UX Flow.

### Epic 7: Local Test Optimization, Admin UI Redesign, and Gameplay Safeguards
Resolve local workspace media upload directories, support advanced prize enums (Diagonal, Border), redesign admin control consoles with vertical sorting and layout optimization, and introduce SuperAdmin management tools and game flow constraints.
**FRs covered:** FR1 (prizes), FR2 (headers/PDFs), FR6 (draw guards), FR8 (admin management).

---

## Epic 1: Event Initialization & Ticket Distribution Infrastructure

Goal: To set up the dual-stack project directories, initialize the database schema versions under Flyway control, and build administrative REST endpoints and views to create events, configure prize structures, and export printable batches of unique tickets as PDFs.

### Story 1.1: Multi-Module Project Structure & Flyway Schema Setup
**As a** Developer,
**I want** to initialize the dual-stack workspace structure (Maven backend + Vite/Vue 3 frontend) and set up Flyway migrations,
**So that** database schemas are version-controlled and development starts from a standardized base.

**Acceptance Criteria:**
- **Given** a clean workspace directory
- **When** the standard starter scripts are run
- **Then** directories `bingo-backend` (Spring Boot 4.0.6, Java 21) and `bingo-frontend` (Vue 3, Vuetify, Tailwind v4) are successfully initialized
- **And** Flyway is configured to run database migration `V1__init_schema.sql` on Spring startup, creating tables `events`, `rounds`, `prizes`, and `tickets` with UUID primary keys and indices.

### Story 1.2: Event & Prize Structure Administrative REST API & UI
**As an** Event Coordinator,
**I want** to create an event and specify custom round prizes (e.g., Column, Line, Full Card) in a dashboard,
**So that** I can customize event metadata and gameplay rules.

**Acceptance Criteria:**
- **Given** an initialized backend running on PostgreSQL
- **When** I submit a POST request to `/api/v1/events` containing name, scheduled time, and prize structures
- **Then** the event is successfully persisted in the database and returned in standard REST ApiResponse envelope
- **And** the Admin web view displays the created event details in a high-contrast Material design dashboard.

### Story 1.3: Secure Ticket Grid Generation & Printable PDF Batch Export
**As an** Event Coordinator,
**I want** to generate a custom batch of physical tickets containing unique UUIDs, QR code URL triggers, and download a printable PDF,
**So that** sellers can physically print and distribute them to local players.

**Acceptance Criteria:**
- **Given** an active created event in the system
- **When** I trigger the batch ticket generator for N tickets (e.g., 500)
- **Then** N unique tickets are generated, each containing a randomized 5x5 integer matrix (1-75, center cell at row 3 column 3 is pre-marked as "FREE" with value `0`) and a secure activation token
- **And** a structured printable PDF file is generated containing tickets with unique serials, clear visual cards, and readable activation QR Codes pointing to the activation URL.

---

## Epic 2: Frictionless Ticket Activation & Player Game Lobby

Goal: To implement zero-friction, credentials-free ticket activation for players and establish the core responsive gaming views with embedded video streaming and tutorial overlays.

### Story 2.1: Instant Player Session Activation via QR / Serial
**As a** physical ticket holder,
**I want** to scan my card's QR Code or type the 6-character code on a web landing page,
**So that** I can instantly activate my ticket and join the digital room without setting up a traditional username/password account.

**Acceptance Criteria:**
- **Given** a valid unactivated ticket serial persisted in the database
- **When** I visit the ticket's embedded QR activation URL or type the 6-character serial in the input field
- **Then** the system marks the ticket status as active in the database and generates a temporary, secure player session token
- **And** redirects my browser smoothly into the active player game room, keeping the session active in memory.

### Story 2.2: Dual-Viewport Responsive Player Layout & Onboarding Guide
**As an** activated player,
**I want** to be greeted by a 3-step interactive onboarding guide and view the YouTube live frame and my ticket cards in an accessible responsive layout,
**So that** I can understand how to play and navigate comfortably on mobile, tablet, or desktop screen viewports.

**Acceptance Criteria:**
- **Given** an active player session loaded in the browser
- **When** the player game room is loaded for the first time
- **Then** an interactive 3-step progressive highlight tutorial displays, highlighting the YouTube stream player, the Cartelão area, and the "BINGO!" button
- **And** the page renders responsively: a desktop-first two-column view (left: video/chat; right: 2x2 grid of all 4 active cards) or a mobile-first top-down stack (top: fixed video; middle: mini-map; bottom: carousel displaying exactly 1 active `BingoCard` at a time).

---

## Epic 3: Real-Time Sourced Admin Board & Broadcast Control

Goal: To build the real-time bidirectional WebSocket channel and construct the administrative control board with safety safeguards, latency sync controllers, and emergency overrides.

### Story 3.1: High-Concurrency STOMP WebSocket Messaging Channel
**As an** Admin,
**I want** a secure, low-latency WebSocket connection using STOMP over WebSocket,
**So that** number draws, claims, and administrative actions are broadcasted instantaneously.

**Acceptance Criteria:**
- **Given** a player or administrative web client
- **When** the client initiates a WebSocket connection to `/ws/bingo`
- **Then** a secure real-time session is established with the backend Spring broker using virtual threads
- **And** clients successfully subscribe to `/topic/events/{id}/draws` to receive drawings, keeping messaging latency under 500ms for 95% of active clients.

### Story 3.2: Error-Proof 75-Key Admin Draw Board & Undo Safeguard
**As an** Event Coordinator,
**I want** a visual 75-key grid that requires double-click confirmation or keyboard Enter confirmation to draw numbers, with a prominent "UNDO" button,
**So that** I can record drawn numbers accurately without accidental broadcasts.

**Acceptance Criteria:**
- **Given** an active event round in progress
- **When** I click a number on the Admin grid or use hotkey shortcuts
- **Then** the visual key turns flashing yellow (pending state)
- **When** I click it a second time within 500ms or press Enter
- **Then** the ball number is persisted in the database and broadcasted instantly to the WebSocket draws topic
- **And** clicking the red 'UNDO' button immediately deletes the last draw from the database and broadcasts a retraction event to all players.

### Story 3.3: Administrative Custom Message Broadcasts & Overrides
**As an** Admin,
**I want** to broadcast text messages and force-override individual ticket configurations,
**So that** I can communicate with all players instantly and resolve physical card disputes.

**Acceptance Criteria:**
- **Given** an ongoing live game session
- **When** the Admin submits an announcement via the panel
- **Then** the text is immediately broadcasted over WebSockets and renders as a scrolling ticker at the top of every player's game room
- **And** Admin can use the search bar to locate any ticket UUID and manually mark or override its numbers in the database, broadcasting the update instantly to that specific client.

---

## Epic 4: Synchronized Passive Gameplay & Automated Marking Engine

Goal: To build the core automated gameplay logic, including client-side latency queuing, accessible game cards, near-win alerts, and mobile mini-maps.

### Story 4.1: Client-Side Latency Alignment Engine
**As a** player,
**I want** incoming WebSocket draw events to be queued and visually marked after a delay corresponding to the stream latency,
**So that** my digital cartels mark numbers exactly when they are announced in the YouTube live video.

**Acceptance Criteria:**
- **Given** an Admin-configured stream delay of D seconds (e.g., 10 seconds)
- **When** a WS `BALL_DRAWN` event is received at system timestamp T
- **Then** the client Pinia store intercepts and holds the event in an internal scheduler queue
- **And** the number is only committed and visually marked on the player's card at exactly timestamp T + D.

### Story 4.2: Automated Marking "Cartelão" Cards with accessibility checks
**As a** colorblind player,
**I want** matching numbers on my cards to pulse green and display visual checkmarks when marked,
**So that** I can confirm my ticket states clearly and accessibility requirements are fully satisfied.

**Acceptance Criteria:**
- **Given** a drawn number released from the client-side latency queue
- **When** the number matches any cells in my active cards (Cartelão)
- **Then** the cell triggers a vivid green pulse animation (`#2E7D32`) and permanently displays a checkmark `✓` icon
- **And** the HTML markup updates dynamically to set `aria-label="Number [X], marked green"`, maintaining WCAG AA contrast (4.5:1).

### Story 4.3: Mobile Mini-Map Selector & Glowing "Quase Lá!" Alerts
**As a** mobile player,
**I want** a horizontal mini-map that flashes for off-screen draws and glows gold when any card is 1 number away from winning,
**So that** I can keep track of all 4 cards and switch focus to near-win cards in time.

**Acceptance Criteria:**
- **Given** a player room loaded in a mobile viewport (carousel view)
- **When** a card that is currently off-screen receives a new mark
- **Then** its corresponding `CardMiniMap` thumbnail flashes green and shows a `+1` bubble for 1.5 seconds
- **When** any card reaches a near-win state (exactly 1 number remaining to win the round prize)
- **Then** the thumbnail glows with a pulsing gold border (`#FFD600`), and a sliding banner alerts: `"Card 3 is almost there! Only 1 number left!"`
- **And** tapping the banner slides the carousel to focus on Card 3 automatically.

---

## Epic 5: Secure Server-Side Auditing & Victory Celebration

Goal: To complete the verification loop, implementing backend claims verification, moderator audit dashboards, and victory celebration wrappers.

### Story 5.1: Reactive Bingo Unlock & Server-Side Claims Engine
**As a** player on the verge of winning,
**I want** the "BINGO!" button to unlock dynamically and trigger a server-side audit of my matrix upon submission,
**So that** I can claim my prize securely and prevent client-side hacks.

**Acceptance Criteria:**
- **Given** an active card that matches the active round's winning pattern (e.g., column, line, or full card)
- **When** the final number is marked visually
- **Then** the BINGO button unlocks, changing from gray to pulsing gold, and programmatically shifts screen reader focus
- **When** the player clicks the gold button
- **Then** a secure POST win claim payload is sent to `/api/v1/claims` and verified strictly on the backend by cross-referencing the card matrix against the drawn balls history.

### Story 5.2: Side-by-Side Admin Audit Dashboard & Celebration Loop
**As an** Admin,
**I want** to review a side-by-side audit overlay highlighting matching numbers on a player's card compared to the actual drawn history,
**So that** I can approve the victory and broadcast a full-screen confetti celebration to all active players.

**Acceptance Criteria:**
- **Given** a secure win claim received from a player session
- **When** the Admin opens the win claim alert modal
- **Then** a side-by-side visual audit panel highlights the player's card layout matched against the actual drawn numbers in chronological order
- **When** the Admin clicks "Approve Winner"
- **Then** a full-screen canvas confetti particle animation and victory sound chime is broadcasted instantly to all active player client rooms.

---

## Epic 6: Security, Stabilization & Quality Audit Fixes

Goal: Enforce strict security, data integrity, LGPD compliance, continuous integration automated builds, and frontend UI flow validations.

### Story 6.1: P0 Security Audit Fixes and Data Integrity
**As an** Event Coordinator and Player,
**I want** the platform to be secure and enforce strict data integrity rules,
**So that** unauthorized access is prevented, game state remains consistent, and player data is safely handled.

**Acceptance Criteria:**
- **Given** a generated ticket, **When** a player accesses it, **Then** they must provide Name and Phone number, and the session token must be properly rotated upon re-authentication.
- **Given** a finished round or event, **When** an admin tries to change its status or modify it, **Then** the system must block the action to maintain immutability.
- **Given** public game events (WebSocket), **When** a claim is approved, **Then** the player's name is broadcasted but the sensitive session token must never be exposed.

### Story 6.2: P1 Stabilization and UX Improvements
**As an** Event Coordinator and Player,
**I want** the platform to have a stable, intuitive user experience and secure dependency management,
**So that** I can easily navigate the game, avoid accidental actions, and trust the integrity of the application.

**Acceptance Criteria:**
- **Given** a player accessing the app, **When** they land on the initial screen, **Then** they only need to provide the Ticket Serial (or scan a QRCode) and the serial is proactively validated.
- **Given** a valid ticket serial, **When** the player enters the GameRoom, **Then** a central modal must prompt for Name, Phone (with Brazilian mask), and LGPD consent, blocking access to the board until completed.

### Story 6.3: P2 Quality, Security, and Operations
**As a** Development Team and Operations Engineer,
**I want** to implement automated continuous integration, enforce data privacy (LGPD), and guarantee system scalability under concurrent loads,
**So that** the platform remains stable in production, passes compliance requirements, and prevents race conditions during high-volume events.

**Acceptance Criteria:**
- **Given** a GitHub repository, **When** a pull request or push is made to the main branch, **Then** a GitHub Actions workflow must automatically build the backend, check frontend vulnerabilities, and run all automated E2E tests.
- **Given** the requirement to handle player data, **When** a user registers their Name and Phone, **Then** the platform must have a formally documented LGPD Privacy Policy explaining data minimization, legal basis, and the non-sharing of personal data.

---

## Epic 7: Local Test Optimization, Admin UI Redesign, and Gameplay Safeguards

Goal: Optimize paths for local testing, introduce custom prize patterns, redesign and optimize administrative drawing layouts, and enforce account administration and gameplay state controls.

### Story 7.1: Image Upload Path Resolution & PDF Header Fixes
**As an** Event Coordinator,
**I want** the header image uploads to work robustly during local testing and render correctly inside the generated PDF ticket batches,
**So that** I can customize the visual branding of physical ticket prints without encountering server errors or missing images.

**Acceptance Criteria:**
- **Given** a multipart image file upload request, **When** the admin submits a custom header image, **Then** the backend must save it using standard file operations (`Files.copy`) instead of servlet-container specific transfer methods that fail on relative paths.
- **Given** an uploaded header image, **When** retrieving it via API or generating the ticket PDF batch, **Then** the path must be dynamically resolved relative to the workspace, ensuring the resource loads correctly across different local execution directories.

### Story 7.2: Custom Prize Type & Admin Control Endpoints Validation
**As an** Event Coordinator,
**I want** the administrative round control interface to support advanced winning patterns (diagonal, outer border, inner border) without causing validation errors,
**So that** I can coordinate more diverse game dynamics for players.

**Acceptance Criteria:**
- **Given** the round creation payload, **When** I choose custom winning patterns (Diagonal, Outer Border, Inner Border), **Then** the backend must accept and recognize these types.
- **Given** the admin dashboard dropdown, **When** adding a new round, **Then** options must map directly to valid enum values on the backend instead of outdated values like 'PHYSICAL'.

### Story 7.3: Admin Game Board Redesign & Scroll Optimization
**As an** Event Coordinator,
**I want** the drawing board numbers to be arranged in sequential vertical columns and the event audit panels to be grouped cleanly below,
**So that** I can control draws quickly and verify claims without scanning or scrolling the page continuously.

**Acceptance Criteria:**
- **Given** the 75-key admin board, **When** rendered, **Then** numbers must progress vertically within sub-columns of 5 under their respective letter headers (B: 1-5, 6-10...; I: 16-20...).
- **Given** the Claims and Round History sections, **When** rendered on the event console, **Then** they must sit side-by-side underneath the drawing board with internal scrolling rather than expanding the entire page height.

### Story 7.4: Admin Lifecycle Management & Gameplay Constraint Guardrails
**As a** SuperAdmin,
**I want** to deactivate, delete, or generate password reset codes for administrator accounts and enforce strict state restrictions on rounds,
**So that** unauthorized admin activity is prevented and game flow rules are programmatically enforced.

**Acceptance Criteria:**
- **Given** an administrator account, **When** toggled or deleted by a SuperAdmin, **Then** the backend must block self-deletion/deactivation, invalidate active sessions of the targeted admin, and restrict endpoints to `ROLE_SUPER_ADMIN`.
- **Given** the event game flow, **When** no round is active, **Then** drawing actions (clicks/hotkeys) must be disabled and display a prominent warning banner.
- **Given** a round status change, **When** starting a round, **Then** the system must block starting it if the parent event status is `AGUARDANDO` or not `ATIVO`.

