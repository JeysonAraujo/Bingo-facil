package com.bingoai.backend.game.dto;

import java.time.Instant;

/**
 * Envelope message broadcasted to all clients on the /topic/events/{eventId}/draws topic
 * when the admin draws a new ball.
 *
 * <p>Follows the standard WebSocket message schema defined in architecture.md:
 * <pre>
 * {
 *   "eventType": "BALL_DRAWN",
 *   "timestamp": "2026-05-28T...",
 *   "payload": {
 *     "ballNumber": 42,
 *     "drawnSequence": 15
 *   }
 * }
 * </pre>
 */
public class DrawBallMessage {

    private String eventType;
    private String timestamp;
    private Payload payload;

    public DrawBallMessage() {
    }

    public DrawBallMessage(int ballNumber, int drawnSequence) {
        this(ballNumber, drawnSequence, Instant.now());
    }

    public DrawBallMessage(int ballNumber, int drawnSequence, Instant timestamp) {
        this.eventType = "BALL_DRAWN";
        this.timestamp = timestamp != null ? timestamp.toString() : Instant.now().toString();
        this.payload = new Payload(ballNumber, drawnSequence);
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
        private int ballNumber;
        private int drawnSequence;

        public Payload() {
        }

        public Payload(int ballNumber, int drawnSequence) {
            this.ballNumber = ballNumber;
            this.drawnSequence = drawnSequence;
        }

        public int getBallNumber() {
            return ballNumber;
        }

        public void setBallNumber(int ballNumber) {
            this.ballNumber = ballNumber;
        }

        public int getDrawnSequence() {
            return drawnSequence;
        }

        public void setDrawnSequence(int drawnSequence) {
            this.drawnSequence = drawnSequence;
        }
    }
}
