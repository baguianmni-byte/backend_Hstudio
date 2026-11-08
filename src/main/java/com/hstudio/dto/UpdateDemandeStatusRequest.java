package com.hstudio.dto;

import com.hstudio.model.DemandeStatus;
import jakarta.validation.constraints.NotNull;

/** Contient le nouveau statut demande pour un dossier. */
public record UpdateDemandeStatusRequest(@NotNull DemandeStatus status) {
}
