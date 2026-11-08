package com.hstudio.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Contient identifiant du commercial destinataire. */
public record UpdateDemandeAssignmentRequest(@NotNull UUID commercialId) {
}
