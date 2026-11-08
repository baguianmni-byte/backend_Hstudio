package com.hstudio.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hstudio.dto.DashboardResponse;
import com.hstudio.service.DashboardService;
import com.hstudio.service.UserService;

import lombok.RequiredArgsConstructor;

/** Fournit les indicateurs du tableau de bord. */
@RestController
@RequestMapping("/crm/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    /**
     * Endpoint pour récupérer les indicateurs avec gestion des 3 filtres (Période, Pôle, Commercial)[cite: 1]
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboardMetrics(
            @RequestParam(required = false) String periode,
            @RequestParam(required = false) String servicePole,
            @RequestParam(required = false) UUID commercialId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        var utilisateur = userService.findByEmail(userDetails.getUsername());

        DashboardResponse metrics = dashboardService.obtenirIndicateursTableauDeBord(
                periode, servicePole, commercialId, utilisateur
        );

        return ResponseEntity.ok(metrics);
    }
}