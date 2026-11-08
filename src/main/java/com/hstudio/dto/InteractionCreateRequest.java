package com.hstudio.dto;

import com.hstudio.model.Interaction;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
/** D le contenu un echange ajoute a une demande. */
public record InteractionCreateRequest(
        @NotBlank String interactionType,
        LocalDateTime interactionDate,
        @NotBlank String summary,
        String clientDecision,
        String nextAction,
        LocalDateTime nextActionDate
) {
    /** Convertit les donn re en objet metier. */
    public Interaction toEntity() {
        Interaction interaction = new Interaction();
        interaction.setInteractionType(interactionType);
        interaction.setInteractionDate(interactionDate);
        interaction.setSummary(summary);
        interaction.setClientDecision(clientDecision);
        interaction.setNextAction(nextAction);
        interaction.setNextActionDate(nextActionDate);
        return interaction;
    }
}
