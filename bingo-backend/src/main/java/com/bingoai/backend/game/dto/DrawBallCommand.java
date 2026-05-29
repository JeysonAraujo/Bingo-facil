package com.bingoai.backend.game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Command payload sent by the Admin panel to trigger a new ball draw.
 * Conforms to a clean input boundary, separating entry commands from broadcast envelopes.
 */
public class DrawBallCommand {

    @NotNull(message = "Ball number is required")
    @Min(value = 1, message = "Ball number must be at least 1")
    @Max(value = 75, message = "Ball number cannot exceed 75")
    private Integer ballNumber;

    @NotNull(message = "Drawn sequence is required")
    @Min(value = 1, message = "Drawn sequence must be at least 1")
    private Integer drawnSequence;

    public DrawBallCommand() {
    }

    public DrawBallCommand(Integer ballNumber, Integer drawnSequence) {
        this.ballNumber = ballNumber;
        this.drawnSequence = drawnSequence;
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
}
