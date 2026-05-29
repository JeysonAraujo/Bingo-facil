package com.bingoai.backend.game.dto;

import java.time.Instant;
import java.util.UUID;

public class TicketOverrideMessage {

    private String eventType;
    private String timestamp;
    private Payload payload;

    public TicketOverrideMessage() {
    }

    public TicketOverrideMessage(UUID ticketId, int ballNumber, String action) {
        this.eventType = "TICKET_OVERRIDE";
        this.timestamp = Instant.now().toString();
        this.payload = new Payload(ticketId, ballNumber, action);
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
        private UUID ticketId;
        private int ballNumber;
        private String action;

        public Payload() {
        }

        public Payload(UUID ticketId, int ballNumber, String action) {
            this.ticketId = ticketId;
            this.ballNumber = ballNumber;
            this.action = action;
        }

        public UUID getTicketId() {
            return ticketId;
        }

        public void setTicketId(UUID ticketId) {
            this.ticketId = ticketId;
        }

        public int getBallNumber() {
            return ballNumber;
        }

        public void setBallNumber(int ballNumber) {
            this.ballNumber = ballNumber;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
