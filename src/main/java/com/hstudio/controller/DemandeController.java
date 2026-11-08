package com.hstudio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hstudio.dto.DemandeCreateRequest;
import com.hstudio.dto.DemandeResponse;
import com.hstudio.service.DemandeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Recoit les nouvelles demandes des clients. */
@RestController
@RequestMapping("/demandes")
@RequiredArgsConstructor
public class DemandeController {

    private final DemandeService demandeService;

    /** Convertit les donn re puis cree une demande. */
    @PostMapping // Expose l'endpoint de soumission des demandes
    public ResponseEntity<DemandeResponse> soumettreDemande(@Valid @RequestBody DemandeCreateRequest request) {
        DemandeResponse nouvelleDemande = DemandeResponse.from(
                demandeService.creerDemandeEtAffecter(request.toEntity())
        );
        return new ResponseEntity<>(nouvelleDemande, HttpStatus.CREATED);
    }
}