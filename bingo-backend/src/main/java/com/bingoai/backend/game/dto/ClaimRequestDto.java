package com.bingoai.backend.game.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ClaimRequestDto {

    @NotNull(message = "Ticket ID is required")
    private UUID ticketId;

    @NotNull(message = "Session token is required")
    private String sessionToken;

    @NotNull(message = "Card index is required")
    private Integer cardIndex;

    public ClaimRequestDto() {
    }

    public ClaimRequestDto(UUID ticketId, String sessionToken, Integer cardIndex) {
        this.ticketId = ticketId;
        this.sessionToken = sessionToken;
        this.cardIndex = cardIndex;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public void setTicketId(UUID ticketId) {
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
}
