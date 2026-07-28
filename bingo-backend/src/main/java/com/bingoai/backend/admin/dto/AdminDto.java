package com.bingoai.backend.admin.dto;

import java.util.UUID;

public record AdminDto(
    UUID id,
    String username,
    String email,
    String role,
    boolean active
) {}
