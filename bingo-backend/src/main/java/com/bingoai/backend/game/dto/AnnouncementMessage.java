package com.bingoai.backend.game.dto;

import java.time.Instant;

/**
 * Announcement envelope broadcasted over WebSockets to all players.
 * Fits the standard envelope design.
 */
public class AnnouncementMessage {

    private String eventType;
    private String timestamp;
    private Payload payload;

    public AnnouncementMessage() {
    }

    public AnnouncementMessage(String text) {
        this.eventType = "ANNOUNCEMENT";
        this.timestamp = Instant.now().toString();
        this.payload = new Payload(text);
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
        private String text;

        public Payload() {
        }

        public Payload(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
