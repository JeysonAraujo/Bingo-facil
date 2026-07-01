---
title: 'Ticket PDF Formatting and Prizes List'
type: 'feature'
created: '2026-07-01'
status: 'done'
context: []
---

# Ticket PDF Formatting and Prizes List

## Intent

**Problem:** The ticket PDF export has inconsistent layout dimensions (e.g. card table height/width, margins, and header sizes) and uses a single-line custom text header instead of a dedicated prizes list placed next to the QR code and ticket ID.

**Approach:** Update the PDF generation layout in the backend using iText: absolute coordinate positioning for full-width header image (210x55mm) and bottom stub (210x50mm), and strict dimensions for card tables (88x63mm). Modify both frontend and backend to replace the custom text header with a prizes list displayed on the top-right of the ticket.

## Boundaries & Constraints

**Always:**
- Keep page size A4 (210mm x 297mm).
- Ensure header image is exactly 210mm x 55mm at the very top of the page.
- Ensure the BINGO card tables are exactly 88mm x 63mm.
- Ensure the stub (canhoto) is exactly 210mm x 50mm at the bottom of the page, separated by a dotted cut line.
- Center the BINGO tables in the grid cells.

**Ask First:**
- N/A

**Never:**
- Do not let content spill over to multiple pages per ticket.
- Do not change event or ticket schema in the database.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Custom prizes provided | `prizes` = "1st: TV\n2nd: Bike" | Render prizes list wrapped on the right side next to serial and QR. | Draw default message if empty. |
| Cut strip enabled | `hasCutStrip` = true | Dotted cut line at Y=141.73 pt (50mm), stub table written below Y=131.73 pt. | N/A |
| Cut strip disabled | `hasCutStrip` = false | Bottom margin is 20 pt, no dotted line or stub table drawn. | N/A |

## Code Map

- `d:\Users\bingo-ai\bingo-backend\src\main\java\com\bingoai\backend\ticket\PdfGenerator.java` -- Core PDF layout & drawing logic.
- `d:\Users\bingo-ai\bingo-backend\src\main\java\com\bingoai\backend\ticket\TicketController.java` -- Ticket PDF download endpoint mapping.
- `d:\Users\bingo-ai\bingo-frontend\src\stores\admin.ts` -- Pinia store action for downloading ticket PDF.
- `d:\Users\bingo-ai\bingo-frontend\src\components\admin\EventGameConsole.vue` -- Export option inputs and download trigger.

## Tasks & Acceptance

**Execution:**
- [ ] `d:\Users\bingo-ai\bingo-backend\src\main\java\com\bingoai\backend\ticket\PdfGenerator.java` -- Modify margins, absolute layout, header image sizing, card table constraints, cut line coordinates, and prizes field rendering.
- [ ] `d:\Users\bingo-ai\bingo-backend\src\main\java\com\bingoai\backend\ticket\TicketController.java` -- Update query parameter from `customHeader` to `prizes` and pass it to PdfGenerator.
- [ ] `d:\Users\bingo-ai\bingo-frontend\src\stores\admin.ts` -- Adjust action parameters and request query parameter to use `prizes`.
- [ ] `d:\Users\bingo-ai\bingo-frontend\src\components\admin\EventGameConsole.vue` -- Replace text input with a `v-textarea` for prizes, rename references to `pdfPrizes`.

**Acceptance Criteria:**
- Given a ticket list and prizes list, when generating PDF, then the PDF is A4 with a full-width header (210x55mm) at the top, a prizes list right-aligned, centered card tables of 88x63mm, and (if hasCutStrip is true) a 50mm high stub at the bottom.

## Design Notes

No complex architecture changes needed. Just drawing elements at exact iText coordinate system.

## Verification

**Commands:**
- `mvn clean compile` -- Check backend builds successfully.
- `npm run build` -- Check frontend builds successfully.
