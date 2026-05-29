package com.bingoai.backend.ticket;

import java.util.List;
import java.util.UUID;

public class TicketResponseDto {
    private UUID id;
    private UUID eventId;
    private String serial;
    private TicketMatrix matrix;
    private boolean activated;
    private List<Integer> markedOverrides;

    public TicketResponseDto() {}

    public TicketResponseDto(UUID id, UUID eventId, String serial, TicketMatrix matrix, boolean activated, List<Integer> markedOverrides) {
        this.id = id;
        this.eventId = eventId;
        this.serial = serial;
        this.matrix = matrix;
        this.activated = activated;
        this.markedOverrides = markedOverrides;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public TicketMatrix getMatrix() { return matrix; }
    public void setMatrix(TicketMatrix matrix) { this.matrix = matrix; }

    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }

    public List<Integer> getMarkedOverrides() { return markedOverrides; }
    public void setMarkedOverrides(List<Integer> markedOverrides) { this.markedOverrides = markedOverrides; }
}
