package com.bingoai.backend.game.dto;

import java.time.Instant;

/**
 * Envelope message sent by a player to claim a BINGO win.
 * Inbound destination: /app/events/{eventId}/claim (Story 5.x will handle this).
 *
 * <p>Follows the standard WebSocket message schema defined in architecture.md:
 * <pre>
 * {
 *   "eventType": "BINGO_CLAIM",
 *   "timestamp": "2026-05-28T...",
 *   "payload": {
 *     "ticketId": "uuid-string",
 *     "sessionToken": "jwt-token"
 *   }
 * }
 * </pre>
 */
public class BingoClaimMessage {

    private String eventType;
    private String timestamp;
    private Payload payload;

    public BingoClaimMessage() {
    }

    public BingoClaimMessage(String ticketId, String sessionToken) {
        this.eventType = "BINGO_CLAIM";
        this.timestamp = Instant.now().toString();
        this.payload = new Payload(ticketId, sessionToken);
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public static class Payload {
        private String ticketId;
        private String sessionToken;

        public Payload() {
        }

        public Payload(String ticketId, String sessionToken) {
            this.ticketId = ticketId;
            this.sessionToken = sessionToken;
        }

        public String getTicketId() {
            return ticketId;
        }

        public void setTicketId(String ticketId) {
            this.ticketId = ticketId;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }
    }
}
