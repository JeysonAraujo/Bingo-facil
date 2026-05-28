package com.bingoai.backend.event.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class EventResponseDto {
    private UUID id;
    private String title;
    private OffsetDateTime scheduledAt;
    private OffsetDateTime createdAt;
    private List<RoundResponseDto> rounds;

    public EventResponseDto() {
    }

    public EventResponseDto(UUID id, String title, OffsetDateTime scheduledAt, OffsetDateTime createdAt, List<RoundResponseDto> rounds) {
        this.id = id;
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
        this.rounds = rounds;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public OffsetDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(OffsetDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<RoundResponseDto> getRounds() {
        return rounds;
    }

    public void setRounds(List<RoundResponseDto> rounds) {
        this.rounds = rounds;
    }
}
