package com.bingoai.backend.event.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateRoundRequest {

    @NotNull(message = "Round number cannot be null")
    private Integer roundNumber;

    @NotEmpty(message = "Round must contain at least one prize")
    private List<CreatePrizeRequest> prizes;

    public CreateRoundRequest() {
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public List<CreatePrizeRequest> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<CreatePrizeRequest> prizes) {
        this.prizes = prizes;
    }
}
