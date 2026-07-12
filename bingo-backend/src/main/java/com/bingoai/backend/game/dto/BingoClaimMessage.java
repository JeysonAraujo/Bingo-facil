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

    public BingoClaimMessage(String ticketId, String sessionToken, Integer cardIndex, String claimId, String status, String serial) {
        this(ticketId, sessionToken, cardIndex, claimId, status, serial, false);
    }

    public BingoClaimMessage(String ticketId, String sessionToken, Integer cardIndex, String claimId, String status, String serial, boolean automatic) {
        this.eventType = "BINGO_CLAIM";
        this.timestamp = Instant.now().toString();
        this.payload = new Payload(ticketId, sessionToken, cardIndex, claimId, status, serial, automatic);
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
        private Integer cardIndex;
        private String claimId;
        private String status;
        private String serial;
        private boolean automatic;
        private String playerName;

        public Payload() {
        }

        public Payload(String ticketId, String sessionToken, Integer cardIndex, String claimId, String status, String serial) {
            this(ticketId, sessionToken, cardIndex, claimId, status, serial, false);
        }

        public Payload(String ticketId, String sessionToken, Integer cardIndex, String claimId, String status, String serial, boolean automatic) {
            this.ticketId = ticketId;
            this.sessionToken = sessionToken;
            this.cardIndex = cardIndex;
            this.claimId = claimId;
            this.status = status;
            this.serial = serial;
            this.automatic = automatic;
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

        public Integer getCardIndex() {
            return cardIndex;
        }

        public void setCardIndex(Integer cardIndex) {
            this.cardIndex = cardIndex;
        }

        public String getClaimId() {
            return claimId;
        }

        public void setClaimId(String claimId) {
            this.claimId = claimId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSerial() {
            return serial;
        }

        public void setSerial(String serial) {
            this.serial = serial;
        }

        public boolean isAutomatic() {
            return automatic;
        }

        public void setAutomatic(boolean automatic) {
            this.automatic = automatic;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }
    }
}
