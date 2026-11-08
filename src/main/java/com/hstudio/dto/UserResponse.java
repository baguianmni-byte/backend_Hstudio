package com.hstudio.dto;

import java.util.UUID;

/** Presente un utilisateur dans les r API. */
public record UserResponse(UUID id, String name, String email, String role, boolean active) {
}
