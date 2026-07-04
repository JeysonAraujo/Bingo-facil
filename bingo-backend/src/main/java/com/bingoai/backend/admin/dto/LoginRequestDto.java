package com.bingoai.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
    @NotBlank(message = "Username é obrigatório") String username,
    @NotBlank(message = "Senha é obrigatória") String password
) {}
