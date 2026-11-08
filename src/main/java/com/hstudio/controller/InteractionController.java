package com.hstudio.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hstudio.dto.InteractionCreateRequest;
import com.hstudio.dto.InteractionResponse;
import com.hstudio.model.User;
import com.hstudio.service.InteractionService;
import com.hstudio.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Expose ajout et consultation des echanges lies aux demandes. */
@RestController
@RequestMapping("/crm/demandes")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;
    private final UserService userService;

    /** Ajoute un echange apres verification de la demande et son auteur. */
    @PostMapping("/{demandeId}/interactions")
    public ResponseEntity<InteractionResponse> ajouterInteraction(
            @PathVariable UUID demandeId,
            @Valid @RequestBody InteractionCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User utilisateur = userService.findByEmail(userDetails.getUsername());

        InteractionResponse nouvelleInteraction = InteractionResponse.from(
                interactionService.ajouterInteraction(demandeId, request.toEntity(), utilisateur)
        );
        return new ResponseEntity<>(nouvelleInteraction, HttpStatus.CREATED);
    }

    /** Renvoie historique des echanges une demande. */
    @GetMapping("/{demandeId}/interactions")
    public ResponseEntity<List<InteractionResponse>> listerInteractions(@PathVariable UUID demandeId) {
        List<InteractionResponse> interactions = interactionService.obtenirHistoriqueDemande(demandeId).stream()
                .map(InteractionResponse::from)
                .toList();
        return ResponseEntity.ok(interactions);
    }
}