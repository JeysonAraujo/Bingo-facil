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

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private TicketMatrix matrix;

    @Column(nullable = false)
    private boolean activated = false;

    @Column(name = "session_token")
    private String sessionToken;

    @Column(name = "player_name", length = 100)
    private String playerName;

    @Column(name = "player_phone", length = 30)
    private String playerPhone;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "marked_overrides", columnDefinition = "jsonb", nullable = false)
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

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getPlayerPhone() { return playerPhone; }
    public void setPlayerPhone(String playerPhone) { this.playerPhone = playerPhone; }

    public List<Integer> getMarkedOverrides() { return markedOverrides; }
    public void setMarkedOverrides(List<Integer> markedOverrides) { this.markedOverrides = markedOverrides; }
}
