package com.hstudio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Contient adresse requise pour renvoyer un code. */
public record PasswordResetResendRequest(
        @NotBlank @Email String email
) {
}
