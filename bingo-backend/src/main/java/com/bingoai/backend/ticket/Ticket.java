package com.bingoai.backend.ticket;

import com.bingoai.backend.event.Event;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, unique = true, length = 50)
    private String serial;

    @Column(columnDefinition = "jsonb", nullable = false)
    @Convert(converter = TicketMatrixConverter.class)
    private TicketMatrix matrix;

    @Column(nullable = false)
    private boolean activated = false;

    @Column(name = "session_token")
    private String sessionToken;

    @Column(name = "marked_overrides", columnDefinition = "jsonb", nullable = false)
    @Convert(converter = TicketOverridesConverter.class)
    private List<Integer> markedOverrides = new ArrayList<>();

    public Ticket() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public String getSerial() { return serial; }
    public void setSerial(String serial) { this.serial = serial; }

    public TicketMatrix getMatrix() { return matrix; }
    public void setMatrix(TicketMatrix matrix) { this.matrix = matrix; }

    public boolean isActivated() { return activated; }
    public void setActivated(boolean activated) { this.activated = activated; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public List<Integer> getMarkedOverrides() { return markedOverrides; }
    public void setMarkedOverrides(List<Integer> markedOverrides) { this.markedOverrides = markedOverrides; }
}
