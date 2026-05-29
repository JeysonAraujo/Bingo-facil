package com.bingoai.backend.game;

import com.bingoai.backend.event.Round;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "draws", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"round_id", "ball_number"}),
    @UniqueConstraint(columnNames = {"round_id", "drawn_sequence"})
})
public class Draw {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    @Column(name = "ball_number", nullable = false)
    private Integer ballNumber;

    @Column(name = "drawn_sequence", nullable = false)
    private Integer drawnSequence;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public Draw() {
    }

    public Draw(Round round, Integer ballNumber, Integer drawnSequence) {
        this.round = round;
        this.ballNumber = ballNumber;
        this.drawnSequence = drawnSequence;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public Integer getBallNumber() {
        return ballNumber;
    }

    public void setBallNumber(Integer ballNumber) {
        this.ballNumber = ballNumber;
    }

    public Integer getDrawnSequence() {
        return drawnSequence;
    }

    public void setDrawnSequence(Integer drawnSequence) {
        this.drawnSequence = drawnSequence;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
