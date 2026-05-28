---
title: "Product Brief: Bingo Online Regional"
title: "Product Brief: Bingo Online Regional: bingo-ai"
status: "complete"
created: "2026-05-14"
updated: "2026-05-14"
inputs: ["User Brain Dump", "Technical Research"]
---

# Product Brief: Bingo Online Regional: bingo-ai

## Executive Summary

Bingo Online Regional is a hybrid gaming platform designed to modernize traditional regional bingo events. It bridges the gap between physical ticket sales and a high-energy, automated digital experience. By combining a live YouTube stream with real-time automatic ticket marking via WebSockets, the platform eliminates the frustration of manual number tracking and human error for players.

The solution allows local organizers to host large-scale events where physical tickets sold by a regional network of sellers are "activated" online. This model maintains the trusted "local feel" of community bingo while providing the scalability and engagement of a modern web application.

## The Problem

Traditional regional bingo events suffer from three primary pain points:
1.  **High Barrier to Entry for Participants**: Managing multiple physical tickets during a fast-paced live draw is stressful and leads to missed numbers and "invalid" claims.
2.  **Fragmented Experience**: Watching a live stream on one device while manually marking paper tickets on a table is clumsy and disconnects the user from the social energy of the event.
3.  **Verification Bottlenecks**: Organizers often struggle to verify winning claims quickly during a live event, leading to dead air and lost momentum.

Today, people cope by using high-latency social media streams (like Facebook Live) and paper markers, which is prone to errors and lacks professional polish.

## The Solution

We are building a real-time web application (Vue.js frontend, Spring Boot backend) that serves as the "Digital Arena" for the bingo event.

*   **Live Stream Hub**: A unified screen with an embedded YouTube Live player at the top and the user’s "Cartelão" (4-ticket layout) at the bottom.
*   **Auto-Marking Engine**: As the Admin inputs drawn numbers, the system automatically marks all active tickets across the entire network in real-time via WebSockets.
*   **Physical-to-Digital Bridge**: Users validate their physical purchase by scanning a QR code or entering a unique ID, which unlocks their digital tickets for the live session.
*   **Victory Experience**: When a bingo is identified (validated server-side), the player receives a celebratory animation, and the Admin is notified via a verified claim queue for final confirmation.
*   **Managed Interaction**: A moderated text chat for social engagement and direct admin-to-user announcements.

## Technical Considerations

*   **Real-Time Sync**: Leveraging Spring Boot’s STOMP over WebSockets ensures that ticket marking happens instantly as the Admin inputs numbers.
*   **Latency Management**: While YouTube Live has a natural delay (5-15s), the digital system will serve as the "ground truth" for marking and claims, ensuring technical fairness.
*   **Fraud Prevention**: All victory claims are strictly verified on the backend against the immutable ticket database record. Frontend manipulation is purely visual and cannot result in a fraudulent prize claim.
*   **Hybrid PDF Engine**: The system generates high-quality PDF "Cartelões" with unique QR codes for physical printing and distribution.

## What Makes This Different

*   **The Hybrid Trust Model**: Unlike 100% digital bingos, we leverage the existing trust of physical sellers and local events, using digital tools only to *enhance* the play experience.
*   **Zero-Error Play**: Automatic marking ensures no player ever misses a number, allowing them to focus on the social energy of the stream.
*   **Regional Focus**: Designed specifically for the logistics of community events, where offline sales and online play coexist seamlessly.

## Who This Serves

*   **The Regional Player**: Local community members who enjoy the social aspect of bingo but want a modern, error-free way to play.
*   **The Event Admin**: Organizers who need a professional tool to manage events, rules (Line, Column, Full House), and rapid winner verification.
*   **The Registered Seller**: A network of partners who handle physical sales and local distribution.

## Scope

**In Scope for MVP:**
*   Admin portal: Event creation, rule definition, PDF generation (with QR Codes), and live number entry.
*   Player portal: Ticket activation, Live Stream view, and auto-marking cartelas.
*   Real-time "BINGO!" claim and admin verification workflow with victory animations.
*   Limited user-to-admin and user-to-user text chat.

**Out of Scope:**
*   In-app payment processing (handled externally).
*   Automatic number drawing (draws remain manual/physical for transparency).
*   Mobile apps (responsive web only for MVP).

## Vision

In 2-3 years, Bingo Online Regional aims to be the standard platform for community fundraising and local entertainment across the country. We envision adding features like "Multi-Event Syndication" and advanced sponsorship overlays for local businesses to advertise directly within the live-play interface.
