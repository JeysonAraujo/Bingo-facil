---
title: "Product Brief Distillate: Bingo Online Regional"
type: llm-distillate
source: "product-brief-finance-ai.md"
created: "2026-05-14"
purpose: "Token-efficient context for downstream PRD creation"
---

# Product Brief Distillate: Bingo Online Regional (bingo-ai)

## Core Concept & Intent
*   **Hybrid Regional Model**: Physical ticket sales via local sellers combined with real-time digital play via YouTube Live.
*   **Primary Value**: Automated ticket marking eliminates player error and stress during fast-paced draws.
*   **Admin Control**: Manual input of numbers drives the entire real-time digital experience.

## Detailed Requirements Hints
*   **"Cartelão" Layout**: A single PDF sheet/screen containing 4 individual tickets.
*   **Ticket Structure**: 5x5 grid (25 numbers) per ticket with a "Free Space" in the center (index 2,2).
*   **Activation Flow**: User buys physical ticket -> Enters unique ID or scans QR code -> Provides Name/Phone -> Joins live session.
*   **Winning Patterns**: Admin must be able to define patterns per round (e.g., Vertical Line, Horizontal Line, Column, Full House/Bingo).
*   **Claim Interaction**: "BINGO!" button for users. Triggers a celebration animation for the winner and places them in an Admin verification queue.
*   **Social Interaction**: Moderated text chat between users and with the Admin.

## Technical Context & Constraints
*   **Frontend**: Vue.js (reactive state for ticket marking and live player).
*   **Backend**: Java with Spring Boot.
*   **Real-Time**: STOMP over WebSockets for instant data push from Admin to all Clients.
*   **PDF Generation**: Spring Boot must generate PDFs with unique IDs and QR Codes for offline printing.
*   **Video**: Embedded YouTube Live player. Must account for 5-15s stream latency vs real-time system state.
*   **Security**: Server-side verification for all claims is mandatory to prevent frontend manipulation.
*   **Data Model**: Registration maps User + Phone + Validated Ticket ID.

## Operational & Business Logic
*   **Payments**: Handled 100% externally by the Admin. The system does not process money.
*   **Seller Role**: Sellers exist in the system as "Registered Sellers" linked to ticket batches, but they do not require a dedicated management panel (Admin handles PDF distribution).
*   **Admin Verification**: Manual check of claims against the number sequence, with system assistance showing the player's hit state.

## Scope Signals
*   **In-Scope**: Event/Round creation, PDF/QR generation, Live Stream screen, Auto-marking, Verification Queue, Chat.
*   **Out-of-Scope**: Automated drawing, In-app payments, Native Mobile Apps (Web only).

## Open Questions / Risks
*   **Scaling**: WebSocket connection limits for high-concurrency regional events.
*   **Latency Perception**: How to visually sync the "auto-mark" with the delayed audio/video from YouTube.
