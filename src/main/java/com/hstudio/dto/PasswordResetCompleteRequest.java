package com.hstudio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Contient le code re et le nouveau mot de passe. */
public record PasswordResetCompleteRequest(
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{6}", message = "Le code OTP doit contenir 6 chiffres") String code,
        @NotBlank @Size(min = 8, max = 72) String newPassword
) {
}
