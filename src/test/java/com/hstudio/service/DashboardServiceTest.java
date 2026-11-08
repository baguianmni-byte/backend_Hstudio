package com.hstudio.service;

import com.hstudio.dto.DashboardResponse;
import com.hstudio.model.Demande;
import com.hstudio.model.DemandeStatus;
import com.hstudio.model.Role;
import com.hstudio.model.User;
import com.hstudio.repository.DemandeRepository;
import com.hstudio.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** V les indicateurs globaux et individuels du tableau de bord. */
class DashboardServiceTest {

    private final DemandeRepository demandeRepository = mock(DemandeRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final DashboardService dashboardService = new DashboardService(demandeRepository, userRepository);

    /** V que la direction voit les chiffres de toute equipe, y compris les commerciaux inactifs. */
    @Test
    void directorGeneralGetsTeamWideMetricsIncludingInactiveCommercials() {
        User inactiveCommercial = User.builder()
                .id(UUID.randomUUID())
                .firstName("Camille")
                .lastName("Martin")
                .role(Role.Commercial)
                .active(false)
                .build();
        User directorGeneral = User.builder()
                .id(UUID.randomUUID())
                .role(Role.Directeur_General)
                .build();
        Demande assignedActiveCase = new Demande();
        assignedActiveCase.setAssignedCommercial(inactiveCommercial);
        assignedActiveCase.setStatus(DemandeStatus.A_PRENDRE_EN_CHARGE);
        Demande wonCase = new Demande();
        wonCase.setStatus(DemandeStatus.GAGNEE);

        when(demandeRepository.findAll()).thenReturn(List.of(assignedActiveCase, wonCase));
        when(userRepository.findByRole(Role.Commercial)).thenReturn(List.of(inactiveCommercial));

        DashboardResponse metrics = dashboardService.obtenirIndicateursTableauDeBord(
                null,
                null,
                null,
                directorGeneral
        );

        assertThat(metrics.totalDemandes()).isEqualTo(2);
        assertThat(metrics.aPrendreEnCharge()).isEqualTo(1);
        assertThat(metrics.gagnees()).isEqualTo(1);
        assertThat(metrics.kpisParCommercial())
                .singleElement()
                .satisfies(commercialKpi -> {
                    assertThat(commercialKpi.commercialName()).isEqualTo("Camille Martin");
                    assertThat(commercialKpi.totalDemandes()).isEqualTo(1);
                    assertThat(commercialKpi.demandesActives()).isEqualTo(1);
                });
    }
}
