package com.bingoai.backend.game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TicketOverrideCommand {

    @NotNull(message = "Ball number is required")
    @Min(value = 1, message = "Ball number must be at least 1")
    @Max(value = 75, message = "Ball number cannot exceed 75")
    private Integer ballNumber;

    @NotBlank(message = "Action is required (MARK/UNMARK)")
    private String action;

    public TicketOverrideCommand() {
    }

    public TicketOverrideCommand(Integer ballNumber, String action) {
        this.ballNumber = ballNumber;
        this.action = action;
    }

    public Integer getBallNumber() {
        return ballNumber;
    }

    public void setBallNumber(Integer ballNumber) {
        this.ballNumber = ballNumber;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
