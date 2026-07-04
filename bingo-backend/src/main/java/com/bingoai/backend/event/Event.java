package com.bingoai.backend.event;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "stream_delay_seconds", nullable = false)
    private Integer streamDelaySeconds = 0;

    @Column(name = "youtube_url")
    private String youtubeUrl;

    @Column(nullable = false)
    private String status = "AGUARDANDO";

    @Column(name = "header_image_url")
    private String headerImageUrl;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Round> rounds = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private com.bingoai.backend.admin.Admin admin;

    public Event() {
    }

    // Helper methods for bidirectional mapping
    public void addRound(Round round) {
        rounds.add(round);
        round.setEvent(this);
    }

    public void removeRound(Round round) {
        rounds.remove(round);
        round.setEvent(null);
    }

    // Getters and Setters
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

    public List<Round> getRounds() {
        return rounds;
    }

    public void setRounds(List<Round> rounds) {
        this.rounds = rounds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHeaderImageUrl() {
        return headerImageUrl;
    }

    public void setHeaderImageUrl(String headerImageUrl) {
        this.headerImageUrl = headerImageUrl;
    }

    public com.bingoai.backend.admin.Admin getAdmin() {
        return admin;
    }

    public void setAdmin(com.bingoai.backend.admin.Admin admin) {
        this.admin = admin;
    }
}
