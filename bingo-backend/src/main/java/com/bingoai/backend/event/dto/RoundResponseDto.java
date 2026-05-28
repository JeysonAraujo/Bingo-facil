package com.bingoai.backend.event.dto;

import java.util.List;
import java.util.UUID;

public class RoundResponseDto {
    private UUID id;
    private Integer roundNumber;
    private String status;
    private List<PrizeResponseDto> prizes;

    public RoundResponseDto() {
    }

    public RoundResponseDto(UUID id, Integer roundNumber, String status, List<PrizeResponseDto> prizes) {
        this.id = id;
        this.roundNumber = roundNumber;
        this.status = status;
        this.prizes = prizes;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(Integer roundNumber) {
        this.roundNumber = roundNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PrizeResponseDto> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<PrizeResponseDto> prizes) {
        this.prizes = prizes;
    }
}
