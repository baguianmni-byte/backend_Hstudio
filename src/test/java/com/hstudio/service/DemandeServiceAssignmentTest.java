package com.hstudio.service;

import com.hstudio.exception.ForbiddenOperationException;
import com.hstudio.exception.InvalidAssignmentException;
import com.hstudio.model.Demande;
import com.hstudio.model.DemandeStatus;
import com.hstudio.model.Role;
import com.hstudio.model.User;
import com.hstudio.repository.DemandeRepository;
import com.hstudio.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** V les regles affectation des demandes. */
class DemandeServiceAssignmentTest {

    private final DemandeRepository demandeRepository = mock(DemandeRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final DemandeService demandeService = new DemandeService(
            demandeRepository,
            userRepository,
            notificationService
    );

    /** V qu un responsable peut reaffecter un dossier et notifier le nouveau commercial. */
    @Test
    void managerCanReassignDemandAndNotifiesNewCommercial() {
        UUID demandId = UUID.randomUUID();
        UUID commercialId = UUID.randomUUID();
        Demande demande = new Demande();
        demande.setId(demandId);
        demande.setCompanyName("Entreprise test");
        demande.setStatus(DemandeStatus.EN_COURS_DE_TRAITEMENT);
        User commercial = User.builder()
                .id(commercialId)
                .firstName("Samira")
                .lastName("Diallo")
                .role(Role.Commercial)
                .active(true)
                .build();
        User admin = User.builder().role(Role.Admin).build();
        when(demandeRepository.findById(demandId)).thenReturn(Optional.of(demande));
        when(userRepository.findById(commercialId)).thenReturn(Optional.of(commercial));
        when(demandeRepository.save(demande)).thenReturn(demande);

        Demande updated = demandeService.changerAffectationDemande(demandId, commercialId, admin);

        assertThat(updated.getAssignedCommercial()).isSameAs(commercial);
        assertThat(updated.getAssignedAt()).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(DemandeStatus.EN_COURS_DE_TRAITEMENT);
        verify(notificationService).creerNotification(
                eq(commercial),
                eq("Affectation initiale"),
                eq("Le dossier Entreprise test vous a été attribué."),
                eq(demandId)
        );
    }

    /** V que la direction marketing peut voir et reaffecter un dossier. */
    @Test
    void marketingDirectorCanViewAndReassignCommercialDemand() {
        UUID demandId = UUID.randomUUID();
        UUID commercialId = UUID.randomUUID();
        Demande demande = new Demande();
        demande.setId(demandId);
        demande.setCompanyName("Prospect marketing");
        User commercial = User.builder()
                .id(commercialId)
                .firstName("Amadou")
                .lastName("Traore")
                .role(Role.Commercial)
                .active(true)
                .build();
        User marketingDirector = User.builder().role(Role.Directeur_Marketing).build();
        when(demandeRepository.findById(demandId)).thenReturn(Optional.of(demande));
        when(userRepository.findById(commercialId)).thenReturn(Optional.of(commercial));
        when(demandeRepository.save(demande)).thenReturn(demande);

        Demande updated = demandeService.changerAffectationDemande(demandId, commercialId, marketingDirector);

        assertThat(updated.getAssignedCommercial()).isSameAs(commercial);
        verify(notificationService).creerNotification(
                eq(commercial),
                eq("Affectation initiale"),
                eq("Le dossier Prospect marketing vous a été attribué."),
                eq(demandId)
        );
    }

    /** V que la direction marketing voit les demandes de equipe. */
    @Test
    void marketingDirectorCanSeeCommercialTeamDemands() {
        User marketingDirector = User.builder().role(Role.Directeur_Marketing).build();
        when(demandeRepository.findAll()).thenReturn(java.util.List.of(new Demande()));

        assertThat(demandeService.obtenirDemandesPourUtilisateur(marketingDirector)).hasSize(1);
        verify(demandeRepository).findAll();
    }

    /** V qu un commercial ne peut pas modifier une affectation. */
    @Test
    void commercialCannotChangeAssignment() {
        User commercial = User.builder().role(Role.Commercial).build();

        assertThatThrownBy(() -> demandeService.changerAffectationDemande(
                UUID.randomUUID(),
                UUID.randomUUID(),
                commercial
        )).isInstanceOf(ForbiddenOperationException.class);

        verify(demandeRepository, never()).findById(org.mockito.ArgumentMatchers.any());
    }

    /** V qu un compte inactif ne peut pas recevoir une affectation. */
    @Test
    void rejectsInactiveCommercialAsAssignmentTarget() {
        UUID demandId = UUID.randomUUID();
        UUID commercialId = UUID.randomUUID();
        Demande demande = new Demande();
        User inactiveCommercial = User.builder()
                .id(commercialId)
                .role(Role.Commercial)
                .active(false)
                .build();
        User directorGeneral = User.builder().role(Role.Directeur_General).build();
        when(demandeRepository.findById(demandId)).thenReturn(Optional.of(demande));
        when(userRepository.findById(commercialId)).thenReturn(Optional.of(inactiveCommercial));

        assertThatThrownBy(() -> demandeService.changerAffectationDemande(
                demandId,
                commercialId,
                directorGeneral
        )).isInstanceOf(InvalidAssignmentException.class);
    }
}
