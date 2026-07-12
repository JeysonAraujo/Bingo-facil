package com.bingoai.backend.event.dto;

import com.bingoai.backend.event.EventStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PublicEventResponseDto(
        UUID id,
        String title,
        OffsetDateTime scheduledAt,
        OffsetDateTime createdAt,
        Integer streamDelaySeconds,
        String youtubeUrl,
        EventStatus status,
        String headerImageUrl,
        List<RoundResponseDto> rounds
) {}
