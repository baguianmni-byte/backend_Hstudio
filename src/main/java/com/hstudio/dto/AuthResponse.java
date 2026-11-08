package com.hstudio.dto;

import com.hstudio.model.Role;

/** Donnees renvoyees apres une authentification reussie. */
public record AuthResponse(
        String token,
        String email,
        Role role,
        String firstName,
        String lastName
) {
}
