package com.hstudio.dto;

import com.hstudio.model.Interaction;

import java.time.LocalDateTime;
import java.util.UUID;
/** Presente un echange associ a une demande. */
public record InteractionResponse(
        UUID id,
        UUID demandeId,
        UserSummary author,
        String interactionType,
        LocalDateTime interactionDate,
        String summary,
        String clientDecision,
        String nextAction,
        LocalDateTime nextActionDate,
        LocalDateTime createdAt
) {
    /** Convertit les donn metier en objet de r API. */
    public static InteractionResponse from(Interaction interaction) {
        return new InteractionResponse(
                interaction.getId(),
                interaction.getDemande().getId(),
                UserSummary.from(interaction.getAuthor()),
                interaction.getInteractionType(),
                interaction.getInteractionDate(),
                interaction.getSummary(),
                interaction.getClientDecision(),
                interaction.getNextAction(),
                interaction.getNextActionDate(),
                interaction.getCreatedAt()
        );
    }
}
