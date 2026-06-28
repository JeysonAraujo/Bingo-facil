package com.bingoai.backend.game.dto;

import java.time.Instant;
import java.util.UUID;

public class RoundStatusMessage {
    private String eventType = "ROUND_STATUS_CHANGED";
    private UUID roundId;
    private int roundNumber;
    private String status;
    private String timestamp = Instant.now().toString();

    public RoundStatusMessage() {
    }

    public RoundStatusMessage(UUID roundId, int roundNumber, String status) {
        this.roundId = roundId;
        this.roundNumber = roundNumber;
        this.status = status;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public UUID getRoundId() {
        return roundId;
    }

    public void setRoundId(UUID roundId) {
        this.roundId = roundId;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
