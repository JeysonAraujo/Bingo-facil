package com.bingoai.backend.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public class CreateEventRequest {

    @NotBlank(message = "Event title cannot be blank")
    private String title;

    @NotNull(message = "Scheduled time cannot be null")
    private OffsetDateTime scheduledAt;

    @NotEmpty(message = "Event must contain at least one round")
    private List<CreateRoundRequest> rounds;

    public CreateEventRequest() {
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

    public List<CreateRoundRequest> getRounds() {
        return rounds;
    }

    public void setRounds(List<CreateRoundRequest> rounds) {
        this.rounds = rounds;
    }
}
