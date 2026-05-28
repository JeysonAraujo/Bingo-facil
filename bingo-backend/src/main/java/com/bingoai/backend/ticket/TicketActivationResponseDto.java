package com.bingoai.backend.ticket;

import java.util.UUID;

public class TicketActivationResponseDto {

    private UUID ticketId;
    private String sessionToken;
    private String serial;
    private TicketMatrix matrix;

    public TicketActivationResponseDto() {}

    public TicketActivationResponseDto(UUID ticketId, String sessionToken, String serial, TicketMatrix matrix) {
        this.ticketId = ticketId;
        this.sessionToken = sessionToken;
        this.serial = serial;
        this.matrix = matrix;
    }

    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public TicketMatrix getMatrix() { return matrix; }
    public void setMatrix(TicketMatrix matrix) { this.matrix = matrix; }
}
