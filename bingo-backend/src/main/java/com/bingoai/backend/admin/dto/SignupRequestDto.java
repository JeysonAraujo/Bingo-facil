package com.bingoai.backend.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequestDto(
    @NotBlank(message = "Username é obrigatório") String username,
    @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
    @NotBlank(message = "Senha é obrigatória") @Size(min = 4, message = "A senha deve ter no mínimo 4 caracteres") String password,
    @NotBlank(message = "Papel é obrigatório") String role
) {}
