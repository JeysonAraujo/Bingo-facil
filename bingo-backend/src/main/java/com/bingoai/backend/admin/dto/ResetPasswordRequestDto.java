package com.bingoai.backend.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDto(
    @NotBlank(message = "E-mail é obrigatório") @Email(message = "E-mail inválido") String email,
    @NotBlank(message = "Código é obrigatório") String code,
    @NotBlank(message = "Nova senha é obrigatória") @Size(min = 4, message = "A senha deve ter no mínimo 4 caracteres") String newPassword
) {}
