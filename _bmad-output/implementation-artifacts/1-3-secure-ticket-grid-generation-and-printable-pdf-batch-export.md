# Story 1.3: Secure Ticket Grid Generation & Printable PDF Batch Export

Status: done

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As an Event Coordinator,
I want to generate a custom batch of physical tickets containing unique UUIDs, QR code URL triggers, and download a printable PDF,
so that sellers can physically print and distribute them to local players.

## Acceptance Criteria

1. **Given** an active created event in the system
2. **When** I trigger the batch ticket generator for N tickets (e.g., 500)
3. **Then** N unique tickets (cartelões) are generated and persisted in the database.
4. **And** each ticket (cartelão) contains a unique UUID, a unique 6-character alphanumeric serial, and a `matrix` field containing 4 distinct cards (cartelas).
5. **And** each of the 4 cards contains a randomized 5x5 integer matrix according to standard 75-ball bingo rules:
   - Column 1 (B): 5 numbers from 1 to 15.
   - Column 2 (I): 5 numbers from 16 to 30.
   - Column 3 (N): 4 numbers from 31 to 45 (center cell at row 3 column 3 is pre-marked as "FREE" with value `0`).
   - Column 4 (G): 5 numbers from 46 to 60.
   - Column 5 (O): 5 numbers from 61 to 75.
   - All numbers in a single card must be unique and sorted vertically within each column.
6. **And** I can download a structured, printable PDF file containing all generated tickets for the event.
7. **And** each PDF page displays exactly 1 ticket (cartelão) with its unique serial, a visual layout of the 4 cards, and a readable activation QR Code pointing to the player activation URL (e.g., `http://localhost:3000/activate?serial=XXXXXX`).

## Tasks / Subtasks

- [x] **Task 1: Add PDF and QR Code Dependencies** (AC: 6, 7)
  - [x] Add `openpdf` (LibrePDF) to `pom.xml` for PDF generation.
  - [x] Add `zxing` (Zebra Crossing) core and javase to `pom.xml` for QR code generation.
- [x] **Task 2: Database Schema Mapping & Ticket Entity** (AC: 3, 4, 5)
  - [x] Create `Ticket.java` entity class in package `com.bingoai.backend.ticket`.
  - [x] Define helper POJOs for the grid matrix: `TicketMatrix` and `CardMatrix`.
  - [x] Implement a JPA Attribute Converter `TicketMatrixConverter` using Jackson to map the custom Java matrix object to/from the PostgreSQL `jsonb` table column.
  - [x] Create `TicketRepository.java` to handle database operations for tickets.
- [x] **Task 3: Ticket Grid Generator Logic** (AC: 4, 5)
  - [x] Write generator logic to build unique 6-character alphanumeric serials (avoiding visually ambiguous characters).
  - [x] Implement standard 75-ball bingo card generation: 5 columns (B, I, N, G, O) with correct ranges, sorted vertically, and center FREE spot marked as `0`.
  - [x] Implement batch generation service in `TicketService.java` that generates N tickets, each with 4 randomized cards, and saves them within a single database transaction.
- [x] **Task 4: QR Code & PDF Generation Service** (AC: 6, 7)
  - [x] Implement `QrCodeGenerator.java` utility to output ZXing QR codes as buffered images or byte arrays.
  - [x] Implement `PdfGenerator.java` using OpenPDF to create a beautiful, high-contrast, printable A4 PDF:
    - Page Header: Event title, Ticket Serial, and QR code to activate the ticket.
    - Card Grid: The 4 cards arranged elegantly in a 2x2 grid.
    - Card Styles: High-contrast borders, headers (B, I, N, G, O), clear number spacing, and a highlighted center "FREE" cell.
- [x] **Task 5: REST API Controller & UI Actions** (AC: 3, 6)
  - [x] Create `TicketController.java` to expose:
    - `POST /api/v1/events/{id}/tickets/generate?quantity=N` (Admin triggers batch generation).
    - `GET /api/v1/events/{id}/tickets/pdf` (Admin downloads the printable A4 PDF).
  - [x] Integrate these actions on the frontend in `admin.ts` store and add a "Gerar Cartelas" button and a "Baixar PDF" link in `AdminDashboard.vue` to allow event coordinators to manage ticket batching.

## Dev Notes

- **Technology Stack & Versions:**
  - OpenPDF: `com.github.librepdf:openpdf:2.0.3` (or latest stable compatible with JDK 21).
  - ZXing: `com.google.zxing:core:3.5.3` & `javase:3.5.3`.
- **Database Portability:** Using a JPA Attribute Converter with Jackson to map the JSON column ensures H2 in-memory DB (used in dev) and PostgreSQL (used in prod) work flawlessly without custom database-specific casting errors.
- **Visual Contrast & Margins:** The PDF must use high-contrast styling (black text, clear boxes) to ensure legible printing even on basic office printers.

### Project Structure Notes

- **Backend Location:** `{project-root}/bingo-backend`
- **Frontend Location:** `{project-root}/bingo-frontend`
- **Status File Location:** `{project-root}/_bmad-output/implementation-artifacts/sprint-status.yaml`

### References

- **PRD Requirements:** FR2 [Source: _bmad-output/planning-artifacts/prd.md#Functional Requirements]
- **Architecture Decisions:** Split Architecture, Flyway Configuration [Source: _bmad-output/planning-artifacts/architecture.md#Architectural Decisions]
- **UX Design Rules:** High-contrast A11y and clear visual representations [Source: _bmad-output/planning-artifacts/ux-design-specification.md]

## Dev Agent Record

### Agent Model Used

Gemini 3.5 Flash (High)

### Debug Log References

None.

### Completion Notes List

- Implemented standard 75-ball bingo card logic avoiding ambiguous serial characters.
- Added Jackson-based JPA AttributeConverter for JSONB `matrix` column mapping.
- Implemented OpenPDF to generate A4 printables matching high contrast requirements.
- Implemented ZXing to stamp activation QR codes on tickets.
- Connected UI with backend via `TicketController` and `admin.ts` Pinia store.

### File List

- bingo-backend/pom.xml
- bingo-backend/src/main/java/com/bingoai/backend/ticket/CardMatrix.java
- bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketMatrix.java
- bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketMatrixConverter.java
- bingo-backend/src/main/java/com/bingoai/backend/ticket/Ticket.java
- bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketRepository.java
- bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketGenerator.java
- bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketService.java
- bingo-backend/src/main/java/com/bingoai/backend/ticket/QrCodeGenerator.java
- bingo-backend/src/main/java/com/bingoai/backend/ticket/PdfGenerator.java
- bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketController.java
- bingo-backend/src/test/java/com/bingoai/backend/ticket/TicketGeneratorTest.java
- bingo-frontend/src/stores/admin.ts
- bingo-frontend/src/components/admin/AdminDashboard.vue

### Change Log

- Addressed all tasks for Story 1.3: Ticket generation and PDF exporting.

### Review Findings

- [x] [Review][Patch] Endpoint Response Structure for Batch Generation — Return a lightweight summary DTO rather than the full list [bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketController.java:25]
- [x] [Review][Patch] Configurable Activation URL in PDF [bingo-backend/src/main/java/com/bingoai/backend/ticket/PdfGenerator.java:41]
- [x] [Review][Patch] LazyInitializationException & N+1 Query in PDF Generation [bingo-backend/src/main/java/com/bingoai/backend/ticket/PdfGenerator.java:32]
- [x] [Review][Patch] Blank First Page in PDF [bingo-backend/src/main/java/com/bingoai/backend/ticket/PdfGenerator.java:29]
- [x] [Review][Patch] Collision Risk & Duplicate Serials in Batch [bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketService.java:37]
- [x] [Review][Patch] Missing UI Loading State and Feedback [bingo-frontend/src/components/admin/AdminDashboard.vue:136]
- [x] [Review][Patch] Inefficient Sublist Reference Sorting [bingo-backend/src/main/java/com/bingoai/backend/ticket/TicketGenerator.java:39]
