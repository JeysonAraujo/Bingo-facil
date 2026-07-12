package com.bingoai.backend.event.dto;

import com.bingoai.backend.event.EventStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class EventResponseDto {
    private UUID id;
    private String title;
    private OffsetDateTime scheduledAt;
    private OffsetDateTime createdAt;
    private Integer streamDelaySeconds;
    private String youtubeUrl;
    private List<RoundResponseDto> rounds;
    private EventStatus status;
    private String headerImageUrl;
    private String adminUsername;

    public EventResponseDto() {
    }

    public EventResponseDto(UUID id, String title, OffsetDateTime scheduledAt, OffsetDateTime createdAt, List<RoundResponseDto> rounds) {
        this(id, title, scheduledAt, createdAt, 0, null, com.bingoai.backend.event.EventStatus.AGUARDANDO, null, rounds, null);
    }

    public EventResponseDto(UUID id, String title, OffsetDateTime scheduledAt, OffsetDateTime createdAt, Integer streamDelaySeconds, List<RoundResponseDto> rounds) {
        this(id, title, scheduledAt, createdAt, streamDelaySeconds, null, com.bingoai.backend.event.EventStatus.AGUARDANDO, null, rounds, null);
    }

    public EventResponseDto(UUID id, String title, OffsetDateTime scheduledAt, OffsetDateTime createdAt, Integer streamDelaySeconds, String youtubeUrl, EventStatus status, String headerImageUrl, List<RoundResponseDto> rounds, String adminUsername) {
        this.id = id;
        this.title = title;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
        this.streamDelaySeconds = streamDelaySeconds;
        this.youtubeUrl = youtubeUrl;
        this.status = status;
        this.headerImageUrl = headerImageUrl;
        this.rounds = rounds;
        this.adminUsername = adminUsername;
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

    public Integer getStreamDelaySeconds() {
        return streamDelaySeconds;
    }

    public void setStreamDelaySeconds(Integer streamDelaySeconds) {
        this.streamDelaySeconds = streamDelaySeconds;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public List<RoundResponseDto> getRounds() {
        return rounds;
    }

    public void setRounds(List<RoundResponseDto> rounds) {
        this.rounds = rounds;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public String getHeaderImageUrl() {
        return headerImageUrl;
    }

    public void setHeaderImageUrl(String headerImageUrl) {
        this.headerImageUrl = headerImageUrl;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
}
