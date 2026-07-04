package com.bingoai.backend.admin.dto;

public record AuthResponseDto(
    String token,
    String username,
    String role
) {}
