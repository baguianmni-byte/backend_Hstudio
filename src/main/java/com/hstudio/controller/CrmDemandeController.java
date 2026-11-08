package com.hstudio.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hstudio.dto.DemandeResponse;
import com.hstudio.dto.UpdateDemandeAssignmentRequest;
import com.hstudio.dto.UpdateDemandeStatusRequest;
import com.hstudio.model.User;
import com.hstudio.service.DemandeService;
import com.hstudio.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Expose la consultation et la gestion des demandes du CRM. */
@RestController
@RequestMapping("/crm/demandes")
@RequiredArgsConstructor
public class CrmDemandeController {

    private final DemandeService demandeService;
    private final UserService userService;

    /**
     * 1. Consulter les demandes selon le rôle (Commercial vs Responsable)[cite: 1]
     */
    @GetMapping
    public ResponseEntity<List<DemandeResponse>> listerDemandes(@AuthenticationPrincipal UserDetails userDetails) {
        User utilisateur = userService.findByEmail(userDetails.getUsername());

        List<DemandeResponse> demandes = demandeService.obtenirDemandesPourUtilisateur(utilisateur).stream()
                .map(DemandeResponse::from)
                .toList();
        return ResponseEntity.ok(demandes);
    }

    /**
     * 2. Modifier le statut d'une demande dans le pipeline commercial[cite: 1]
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<DemandeResponse> modifierStatut(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDemandeStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User utilisateur = userService.findByEmail(userDetails.getUsername());

        DemandeResponse demandeMiseAJour = DemandeResponse.from(
                demandeService.changerStatutDemande(id, request.status(), utilisateur)
        );

        return ResponseEntity.ok(demandeMiseAJour);
    }

    /** Transmet la reaffectation du dossier au service metier. */
    @PatchMapping("/{id}/assignment")
    public ResponseEntity<DemandeResponse> modifierAffectation(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDemandeAssignmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User utilisateur = userService.findByEmail(userDetails.getUsername());
        DemandeResponse demandeMiseAJour = DemandeResponse.from(
                demandeService.changerAffectationDemande(id, request.commercialId(), utilisateur)
        );
        return ResponseEntity.ok(demandeMiseAJour);
    }
}