package com.hstudio.service;

import com.hstudio.exception.ForbiddenOperationException;
import com.hstudio.exception.InvalidAssignmentException;
import com.hstudio.exception.ResourceNotFoundException;
import com.hstudio.model.Demande;
import com.hstudio.model.DemandeStatus;
import com.hstudio.model.Role;
import com.hstudio.model.User;
import com.hstudio.repository.DemandeRepository;
import com.hstudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** G les demandes, leurs affectations et les notifications associ. */
@Service
@RequiredArgsConstructor
public class DemandeService {

    private static final Set<DemandeStatus> STATUTS_TERMINAUX = Set.of(
            DemandeStatus.GAGNEE,
            DemandeStatus.PERDUE,
            DemandeStatus.CLOTUREE_SANS_SUITE
    );

    private final DemandeRepository demandeRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService; // Injection du service de notification

    /** Enregistre une demande, choisit un commercial et notifie les acteurs. */
    @Transactional
    public Demande creerDemandeEtAffecter(Demande nouvelleDemande) {
        // 1. Enregistrement initial de la demande
        nouvelleDemande.setStatus(DemandeStatus.NOUVELLE_DEMANDE);
        
        // 2. Application de l'algorithme d'affectation automatique
        User commercialAssigne = selectionnerCommercialPourAffectation();
        if (commercialAssigne != null) {
            nouvelleDemande.setAssignedCommercial(commercialAssigne);
            nouvelleDemande.setAssignedAt(LocalDateTime.now());
            nouvelleDemande.setStatus(DemandeStatus.A_PRENDRE_EN_CHARGE);
        }

        Demande demandeSauvegardee = demandeRepository.save(nouvelleDemande);

        // 3. Déclenchement des notifications selon le cahier des charges (Partie 2.6.2)
        notifierActeursNouvelleDemande(demandeSauvegardee, commercialAssigne);

        return demandeSauvegardee;
    }

    /** V les droits, modifie le statut et notifie le responsable. */
    @Transactional
    public Demande changerStatutDemande(UUID demandeId, DemandeStatus nouveauStatut, User utilisateurConnecte) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable avec l'ID : " + demandeId));

        // Vérification des droits
        boolean isResponsable = estResponsable(utilisateurConnecte);
        boolean isAssignedCommercial = demande.getAssignedCommercial() != null && 
                                       demande.getAssignedCommercial().getId().equals(utilisateurConnecte.getId());

        if (!isResponsable && !isAssignedCommercial) {
            throw new ForbiddenOperationException("Vous ne pouvez modifier que les demandes qui vous sont attribuées.");
        }

        DemandeStatus ancienStatut = demande.getStatus();
        demande.setStatus(nouveauStatut);
        Demande demandeMiseAJour = demandeRepository.save(demande);

        // Notification du premier responsable en cas de changement de statut (Partie 2.6.2)[cite: 1]
        if (ancienStatut != nouveauStatut) {
            notifierResponsableChangementStatut(demandeMiseAJour, nouveauStatut);
        }

        return demandeMiseAJour;
    }

    /** V la cible puis attribue le dossier a un commercial actif. */
    @Transactional
    public Demande changerAffectationDemande(UUID demandeId, UUID commercialId, User utilisateurConnecte) {
        if (!estResponsable(utilisateurConnecte)) {
            throw new ForbiddenOperationException("Seuls les responsables et le Directeur marketing peuvent modifier l'affectation.");
        }

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable avec l'ID : " + demandeId));
        User commercial = userRepository.findById(commercialId)
                .orElseThrow(() -> new ResourceNotFoundException("Commercial introuvable avec l'ID : " + commercialId));

        if (commercial.getRole() != Role.Commercial || !commercial.isActive()) {
            throw new InvalidAssignmentException("La demande doit être affectée à un commercial actif.");
        }

        User ancienCommercial = demande.getAssignedCommercial();
        if (ancienCommercial != null && ancienCommercial.getId().equals(commercial.getId())) {
            return demande;
        }

        demande.setAssignedCommercial(commercial);
        demande.setAssignedAt(LocalDateTime.now());
        if (demande.getStatus() == DemandeStatus.NOUVELLE_DEMANDE) {
            demande.setStatus(DemandeStatus.A_PRENDRE_EN_CHARGE);
        }

        Demande demandeMiseAJour = demandeRepository.save(demande);
        notificationService.creerNotification(
                commercial,
                "Affectation initiale",
                "Le dossier " + demande.getCompanyName() + " vous a été attribué.",
                demande.getId()
        );
        return demandeMiseAJour;
    }

    /** Renvoie les demandes selon le role de utilisateur. */
    public List<Demande> obtenirDemandesPourUtilisateur(User utilisateur) {
        if (estResponsable(utilisateur)) {
            return demandeRepository.findAll();
        } else {
            return demandeRepository.findByAssignedCommercial(utilisateur);
        }
    }

    // --- Logique d'affectation automatique (Partie 2.5.2)[cite: 1] ---
    /** Choisit un commercial selon sa charge et ses dernis affectations. */
    private User selectionnerCommercialPourAffectation() {
        List<User> commerciaux = userRepository.findByRoleAndActiveTrue(Role.Commercial);
        if (commerciaux.isEmpty()) {
            return null;
        }

        // À charge égale, privilégie les dossiers en attente du client, puis le commercial le moins récemment sollicité.
        return commerciaux.stream()
            .min(Comparator
                .comparingLong((User c) -> demandeRepository.countByAssignedCommercialAndStatusNotIn(c, STATUTS_TERMINAUX))
                .thenComparing(Comparator.comparingLong(
                        (User c) -> demandeRepository.countByAssignedCommercialAndStatus(c, DemandeStatus.EN_ATTENTE_CLIENT)
                ).reversed())
                .thenComparing(Comparator.comparing(
                        this::getDerniereAttribution,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ))
            )
            .orElse(commerciaux.get(0));
    }

    /** Trouve la date de derni affectation du commercial. */
    private LocalDateTime getDerniereAttribution(User commercial) {
        return demandeRepository.findByAssignedCommercial(commercial).stream()
                .map(Demande::getAssignedAt)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    // --- Gestion interne des notifications (Partie 2.6.2)[cite: 1] ---
    /** Envoie les notifications prevues aux commerciaux et responsables actifs. */
    private void notifierActeursNouvelleDemande(Demande demande, User commercialAssigne) {
        List<User> responsables = obtenirResponsablesActifs();
        List<User> commerciaux = userRepository.findByRoleAndActiveTrue(Role.Commercial);

        // 1. Notification "Nouvelle demande reçue" -> Tous les commerciaux et le premier responsable[cite: 1]
        for (User comm : commerciaux) {
            notificationService.creerNotification(
                comm,
                "Nouvelle demande reçue",
                "Une nouvelle demande de " + demande.getCompanyName() + " a été enregistrée.",
                demande.getId()
            );
        }

        for (User resp : responsables) {
            notificationService.creerNotification(
                resp,
                "Nouvelle demande reçue",
                "Une nouvelle demande de " + demande.getCompanyName() + " a été enregistrée.",
                demande.getId()
            );

            // 2. Notification "Affectation initiale" -> Premier responsable et commercial désigné[cite: 1]
            if (commercialAssigne != null) {
                notificationService.creerNotification(
                    resp,
                    "Affectation initiale",
                    "La demande #" + demande.getId() + " a été affectée à " + commercialAssigne.getFirstName() + " " + commercialAssigne.getLastName() + ".",
                    demande.getId()
                );
            }
        }

        if (commercialAssigne != null) {
            notificationService.creerNotification(
                commercialAssigne,
                "Affectation initiale",
                "Vous avez été désigné responsable de la nouvelle demande de " + demande.getCompanyName() + ".",
                demande.getId()
            );
        }
    }

    /** Informe le responsable actif du nouveau statut de la demande. */
    private void notifierResponsableChangementStatut(Demande demande, DemandeStatus nouveauStatut) {
        List<User> responsables = obtenirResponsablesActifs();
        // Notification "Changement de statut" -> Premier responsable commercial[cite: 1]
        for (User resp : responsables) {
            notificationService.creerNotification(
                resp,
                "Changement de statut",
                "La demande #" + demande.getId() + " (" + demande.getCompanyName() + ") est passée au statut : " + nouveauStatut.name(),
                demande.getId()
            );
        }
    }

    /** V si le role autorise la gestion des demandes. */
    private boolean estResponsable(User utilisateur) {
        return utilisateur.getRole() == Role.Admin
                || utilisateur.getRole() == Role.Directeur_General
                || utilisateur.getRole() == Role.Directeur_Marketing;
    }

    /** Recherche les responsables qui peuvent recevoir les notifications. */
    private List<User> obtenirResponsablesActifs() {
        List<User> responsables = userRepository.findByRoleAndActiveTrue(Role.Admin);
        responsables.addAll(userRepository.findByRoleAndActiveTrue(Role.Directeur_General));
        responsables.addAll(userRepository.findByRoleAndActiveTrue(Role.Directeur_Marketing));
        return responsables;
    }
}