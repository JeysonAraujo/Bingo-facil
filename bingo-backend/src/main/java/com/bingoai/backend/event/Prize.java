package com.bingoai.backend.event;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "prizes")
public class Prize {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private Round round;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrizeType type;

    @Column(name = "draw_pattern", nullable = false)
    private String drawPattern;

    @Column(nullable = false)
    private boolean divisible = true;

    public Prize() {
    }

    // Getters and Setters
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PrizeType getType() {
        return type;
    }

    public void setType(PrizeType type) {
        this.type = type;
    }

    public String getDrawPattern() {
        return drawPattern;
    }

    public void setDrawPattern(String drawPattern) {
        this.drawPattern = drawPattern;
    }

    public boolean isDivisible() {
        return divisible;
    }

    public void setDivisible(boolean divisible) {
        this.divisible = divisible;
    }
}
