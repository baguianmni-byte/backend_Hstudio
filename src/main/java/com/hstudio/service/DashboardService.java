package com.hstudio.service;

import com.hstudio.dto.DashboardResponse;
import com.hstudio.dto.CommercialKpi;
import com.hstudio.model.Demande;
import com.hstudio.model.DemandeStatus;
import com.hstudio.model.User;
import com.hstudio.repository.DemandeRepository;
import com.hstudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Calcule les indicateurs avec les droits et filtres applicables. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DemandeRepository demandeRepository;
    private final UserRepository userRepository;

    /**
     * Calcule les 6 indicateurs du tableau de bord avec les filtres optionnels (Partie 2.9)
     */
    public DashboardResponse obtenirIndicateursTableauDeBord(
            String periode, 
            String servicePole, 
            UUID commercialId, 
            User utilisateurConnecte
    ) {
        List<Demande> demandes = demandeRepository.findAll();

        // Application des restrictions de rôles (un commercial ne voit que ses stats, le responsable voit tout)[cite: 1]
        if (utilisateurConnecte.getRole().name().equals("Commercial")) {
            demandes = demandes.stream()
                    .filter(d -> d.getAssignedCommercial() != null && d.getAssignedCommercial().getId().equals(utilisateurConnecte.getId()))
                    .collect(Collectors.toList());
        }

        // Filtre 2 — Pôle ou service
        if (servicePole != null && !servicePole.isEmpty()) {
            demandes = demandes.stream()
                    .filter(d -> servicePole.equalsIgnoreCase(d.getServicePole()))
                    .collect(Collectors.toList());
        }

        // Filtre 3 — Commercial responsable (pour le responsable)
        if (commercialId != null) {
            demandes = demandes.stream()
                    .filter(d -> d.getAssignedCommercial() != null && d.getAssignedCommercial().getId().equals(commercialId))
                    .collect(Collectors.toList());
        }

        // Filtre 1 — Période
        if (periode != null) {
            LocalDateTime maintenant = LocalDateTime.now();
            demandes = demandes.stream().filter(d -> {
                if (d.getCreatedAt() == null) return false;
                if (periode.equalsIgnoreCase("AUJOURDHUI")) {
                    return d.getCreatedAt().toLocalDate().isEqual(maintenant.toLocalDate());
                } else if (periode.equalsIgnoreCase("SEMAINE")) {
                    return d.getCreatedAt().isAfter(maintenant.minusWeeks(1));
                } else if (periode.equalsIgnoreCase("MOIS")) {
                    return d.getCreatedAt().isAfter(maintenant.minusMonths(1));
                }
                return true;
            }).collect(Collectors.toList());
        }

        // Calcul des 6 indicateurs de la première version (Partie 2.9.1)[cite: 1]
        long totalDemandes = demandes.size();
        long nouvellesDemandes = demandes.stream().filter(d -> d.getStatus() == DemandeStatus.NOUVELLE_DEMANDE).count();
        long aPrendreEnCharge = demandes.stream().filter(d -> d.getStatus() == DemandeStatus.NOUVELLE_DEMANDE
                || d.getStatus() == DemandeStatus.A_PRENDRE_EN_CHARGE).count();
        long enCoursTraitement = demandes.stream().filter(d -> d.getStatus() == DemandeStatus.EN_COURS_DE_TRAITEMENT).count();
        long propositionsEnvoyees = demandes.stream().filter(d -> d.getStatus() == DemandeStatus.PROPOSITION_COMMERCIALE_ENVOYEE).count();
        long gagnees = demandes.stream().filter(d -> d.getStatus() == DemandeStatus.GAGNEE).count();
        long perdues = demandes.stream().filter(d -> d.getStatus() == DemandeStatus.PERDUE).count();

        // Indicateurs individuels de chaque commercial, y compris les comptes inactifs.
        List<User> commerciaux = userRepository.findByRole(com.hstudio.model.Role.Commercial);
        List<Demande> demandesFiltrees = demandes;
        List<CommercialKpi> kpisParCommercial = commerciaux.stream()
                .map(commercial -> {
                    List<Demande> demandesCommercial = demandesFiltrees.stream()
                            .filter(demande -> demande.getAssignedCommercial() != null
                                    && demande.getAssignedCommercial().getId().equals(commercial.getId()))
                            .toList();
                    long gagneesCommercial = demandesCommercial.stream()
                            .filter(demande -> demande.getStatus() == DemandeStatus.GAGNEE)
                            .count();
                    long perduesCommercial = demandesCommercial.stream()
                            .filter(demande -> demande.getStatus() == DemandeStatus.PERDUE)
                            .count();
                    long activesCommercial = demandesCommercial.stream()
                            .filter(demande -> demande.getStatus() != DemandeStatus.GAGNEE
                                    && demande.getStatus() != DemandeStatus.PERDUE
                                    && demande.getStatus() != DemandeStatus.CLOTUREE_SANS_SUITE)
                            .count();
                    return new CommercialKpi(
                            commercial.getId(),
                            commercial.getFirstName() + " " + commercial.getLastName(),
                            demandesCommercial.size(),
                            activesCommercial,
                            gagneesCommercial,
                            perduesCommercial
                    );
                })
                .toList();

        // Construction de la réponse JSON
        return new DashboardResponse(
                totalDemandes,
                nouvellesDemandes,
                aPrendreEnCharge,
                enCoursTraitement,
                propositionsEnvoyees,
                gagnees,
                perdues,
                kpisParCommercial
        );
    }
}