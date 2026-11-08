package com.hstudio.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Contient les identifiants transmis pour ouvrir une session. */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
