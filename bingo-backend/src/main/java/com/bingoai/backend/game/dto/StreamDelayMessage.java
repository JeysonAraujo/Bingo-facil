package com.bingoai.backend.game.dto;

import java.time.Instant;

public class StreamDelayMessage {

    private String eventType;
    private String timestamp;
    private Payload payload;

    public StreamDelayMessage() {
    }

    public StreamDelayMessage(int streamDelaySeconds) {
        this.eventType = "DELAY_UPDATED";
        this.timestamp = Instant.now().toString();
        this.payload = new Payload(streamDelaySeconds);
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
        private int streamDelaySeconds;

        public Payload() {
        }

        public Payload(int streamDelaySeconds) {
            this.streamDelaySeconds = streamDelaySeconds;
        }

        public int getStreamDelaySeconds() {
            return streamDelaySeconds;
        }

        public void setStreamDelaySeconds(int streamDelaySeconds) {
            this.streamDelaySeconds = streamDelaySeconds;
        }
    }
}
