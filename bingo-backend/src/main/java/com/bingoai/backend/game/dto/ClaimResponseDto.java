package com.bingoai.backend.game.dto;

import java.util.UUID;

public class ClaimResponseDto {

    private UUID claimId;
    private UUID ticketId;
    private UUID roundId;
    private Integer cardIndex;
    private String status;
    private String createdAt;
    private String serial;
    private boolean automatic;

    public ClaimResponseDto() {
    }

    public ClaimResponseDto(UUID claimId, UUID ticketId, UUID roundId, Integer cardIndex, String status, String createdAt, String serial) {
        this(claimId, ticketId, roundId, cardIndex, status, createdAt, serial, false);
    }

    public ClaimResponseDto(UUID claimId, UUID ticketId, UUID roundId, Integer cardIndex, String status, String createdAt, String serial, boolean automatic) {
        this.claimId = claimId;
        this.ticketId = ticketId;
        this.roundId = roundId;
        this.cardIndex = cardIndex;
        this.status = status;
        this.createdAt = createdAt;
        this.serial = serial;
        this.automatic = automatic;
    }

    public UUID getClaimId() {
        return claimId;
    }

    public void setClaimId(UUID claimId) {
        this.claimId = claimId;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    public UUID getRoundId() {
        return roundId;
    }

    public void setRoundId(UUID roundId) {
        this.roundId = roundId;
    }

    public Integer getCardIndex() {
        return cardIndex;
    }

    public void setCardIndex(Integer cardIndex) {
        this.cardIndex = cardIndex;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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
}
