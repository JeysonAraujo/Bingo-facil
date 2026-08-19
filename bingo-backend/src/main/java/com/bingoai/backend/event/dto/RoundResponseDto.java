package com.bingoai.backend.event.dto;

import java.util.List;
import java.util.UUID;

public class RoundResponseDto {
    private UUID id;
    private Integer roundNumber;
    private com.bingoai.backend.event.RoundStatus status;
    private List<PrizeResponseDto> prizes;
    private String tieBreakerRule;

    public RoundResponseDto() {
    }

    public RoundResponseDto(UUID id, Integer roundNumber, com.bingoai.backend.event.RoundStatus status, List<PrizeResponseDto> prizes) {
        this(id, roundNumber, status, prizes, "SPLIT_PRIZE");
    }

    public RoundResponseDto(UUID id, Integer roundNumber, com.bingoai.backend.event.RoundStatus status, List<PrizeResponseDto> prizes, String tieBreakerRule) {
        this.id = id;
        this.roundNumber = roundNumber;
        this.status = status;
        this.prizes = prizes;
        this.tieBreakerRule = tieBreakerRule;
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

    public com.bingoai.backend.event.RoundStatus getStatus() {
        return status;
    }

    public void setStatus(com.bingoai.backend.event.RoundStatus status) {
        this.status = status;
    }

    public List<PrizeResponseDto> getPrizes() {
        return prizes;
    }

    public void setPrizes(List<PrizeResponseDto> prizes) {
        this.prizes = prizes;
    }

    public String getTieBreakerRule() {
        return tieBreakerRule;
    }

    public void setTieBreakerRule(String tieBreakerRule) {
        this.tieBreakerRule = tieBreakerRule;
    }
}
