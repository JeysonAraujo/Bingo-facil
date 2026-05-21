---
stepsCompleted: ["step-01-init", "step-02-discovery", "step-02b-vision", "step-02c-executive-summary", "step-03-success", "step-04-journeys", "step-05-domain", "step-06-innovation", "step-07-project-type", "step-08-scoping", "step-09-functional", "step-10-nonfunctional", "step-11-polish", "step-12-complete"]
releaseMode: phased
inputDocuments: ["product-brief-bingo-ai.md", "product-brief-bingo-ai-distillate.md"]
workflowType: 'prd'
---

# Product Requirements Document - bingo-ai

**Author:** Jeyson
**Date:** 2026-05-15

## Executive Summary

**bingo-ai** is a hybrid regional bingo platform designed to modernize traditional community events, such as those organized by local churches and regional associations. By integrating physical ticket sales with a real-time digital experience via YouTube Live, the platform addresses core challenges of trust, accessibility, and manual error. It allows users to participate remotely or locally with automated synchronization, preserving the social essence of bingo while removing the logistical friction of manual marking.

### Core Differentiator
The platform’s "Intelligent Real-Time Synchronization" manages streaming latency to ensure automated marking is perfectly timed with the live broadcast. Unlike traditional methods, **bingo-ai** enhances player engagement through emotional triggers, including "near-win" alerts ("Quase lá!") and celebratory rewards (confetti and victory chimes).

## Project Classification & Architecture

- **Project Type:** Full-stack Web Application (Vue.js SPA + Spring Boot)
- **Domain:** Entertainment / Regional Events
- **Complexity:** Medium (Real-time WebSocket sync and stream latency management)
- **Architecture**: Single Page Application (SPA) for a seamless, no-reload game room experience.
- **Backend**: Java Spring Boot to handle high-concurrency (500+) WebSocket connections.

## Success Criteria

### Measurable Outcomes
- **Digital Adoption**: >90% of physical ticket buyers successfully activate and join the live session.
- **System Accuracy**: 100% accuracy in server-side claim verification.
- **Latency**: Ticket state updates in <500ms after an Admin command.
- **Sync Fidelity**: Real-time alignment between auto-marking and YouTube Live audio/video.

### Business & User Success
- **Trust**: Zero cases of fraud or human error in prize validation.
- **Retention**: Local organizers conducting multiple events using the platform.
- **Aha! Moment**: Positive feedback on the stress-free auto-marking and excitement of "Near-win" alerts.

## Project Scoping & Phased Development

### MVP Strategy (Phase 1)
**Focus**: Delivering the core "Physical-Digital Bridge" experience.
- **Physical Integration**: PDF generation with unique IDs and QR codes.
- **Admin Control**: Manual entry interface for drawn numbers (mirrored from physical globe).
- **Live Sync Engine**: Configurable delay to synchronize auto-marking with YouTube Live latency.
- **Real-Time Game Screen**: Embedded video with "Cartelão" (4 tickets) and automated status updates.
- **Verification Engine**: 100% server-side validation of prize claims.

### Growth & Vision (Phases 2 & 3)
- **Phase 2 (Growth)**: Moderated player chat, management of multiple prizes per round (Line, Column), and a Basic Seller Dashboard.
- **Phase 3 (Vision)**: Advanced event analytics, local sponsor integration (ad slots), and simplified audit log access.

## User Journeys

### Maria: The "Stress-Free" Player
Maria scans her physical ticket's QR code on her phone. During the live stream, she watches the YouTube video while her digital "Cartelão" marks itself automatically. When one number remains, her screen glows ("Quase lá!"). Upon winning, she clicks a giant "BINGO!" button, triggering confetti and alerting the Admin for verification.

### Sr. José: The Confident Admin
Sr. José calls numbers from a physical globe and clicks them on his control panel. He adjusts a 10-second delay to match the YouTube stream's latency. When a "Bingo!" is claimed, he sees a verified proof of the ticket state and confirms the winner instantly, avoiding disputes.

### Roberto: The Trusted Seller
Roberto sells physical tickets in the town square. He uses the digital auto-marking feature as a selling point: "Just scan this, and it marks itself—no pen needed!"

## Technical & Domain Requirements

### Compliance & Synchronization
- **Audit Logging**: Comprehensive logs of ticket activation, number draws, and prize claims.
- **Latency Sync**: Configurable client-side delay logic (0-30s) to match stream latency.
- **Concurrency**: Robust WebSocket handling for burst traffic and high simultaneous connections.

### Innovation & Risks
- **Emotional Synchronization**: An engine that detects "near-win" states to trigger engagement alerts.
- **Risk Mitigation**: Double-confirmation UI for Admin entries to prevent manual errors; "Undo" functionality for the last called number.

## Functional Requirements

### Event & Ticket Management
- **FR1**: Admin can create events and manage prize structures for each round.
- **FR2**: Admin can generate unique ticket IDs, QR codes, and printable PDF batches.

### User Activation & Experience
- **FR3**: Users can activate tickets via QR scan or ID entry.
- **FR4**: Users can view a synchronized YouTube Live stream and digital "Cartelão" simultaneously.
- **FR5**: System automatically marks tickets and triggers "near-win" and "victory" animations.

### Admin Control & Validation
- **FR6**: Admin can manually record drawn numbers and adjust stream synchronization delay.
- **FR7**: System verifies "Bingo!" claims on the server and provides visual proof for Admin confirmation.
- **FR8**: Admin can broadcast messages and override ticket data in real-time.

## Non-Functional Requirements

- **Latency**: WebSocket messages must reach 95% of clients within 500ms.
- **Security**: All player data encrypted at rest and in transit; server-side-only verification for all wins.
- **Accessibility**: High-contrast UI (4.5:1 ratio) and large touch targets (44x44px) for elderly users.
- **Reliability**: 99.9% uptime during event windows; real-time state persistence for crash recovery.
