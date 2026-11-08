package com.hstudio.dto;

import com.hstudio.model.User;

import java.util.UUID;
/** Fournit un resume utilisateur pour les objets de r. */
public record UserSummary(UUID id, String name) {
    /** Convertit les donn metier en objet de r API. */
    public static UserSummary from(User user) {
        if (user == null) {
            return null;
        }
        return new UserSummary(user.getId(), user.getFirstName() + " " + user.getLastName());
    }
}
