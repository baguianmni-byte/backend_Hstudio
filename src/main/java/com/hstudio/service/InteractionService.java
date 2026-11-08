package com.hstudio.service;

import com.hstudio.exception.ResourceNotFoundException;
import com.hstudio.model.Demande;
import com.hstudio.model.Interaction;
import com.hstudio.model.User;
import com.hstudio.repository.DemandeRepository;
import com.hstudio.repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** G les echanges lies aux demandes. */
@Service
@RequiredArgsConstructor
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final DemandeRepository demandeRepository;

    /** Ajoute un echange apres verification de la demande et son auteur. */
    @Transactional
    public Interaction ajouterInteraction(UUID demandeId, Interaction interaction, User auteur) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable avec l'ID : " + demandeId));
        
        interaction.setDemande(demande);
        interaction.setAuthor(auteur); // Conserve l'auteur de l'enregistrement
        return interactionRepository.save(interaction);
    }

    /** Renvoie les echanges associs a la demande. */
    public List<Interaction> obtenirHistoriqueDemande(UUID demandeId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable avec l'ID : " + demandeId));
        return interactionRepository.findByDemande(demande);
    }
}