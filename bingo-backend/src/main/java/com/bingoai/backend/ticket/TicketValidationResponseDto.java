package com.bingoai.backend.ticket;

import java.util.UUID;

public record TicketValidationResponseDto(
        String serial,
        UUID eventId,
        String eventTitle,
        String headerImageUrl,
        boolean activated
) {}
