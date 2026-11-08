package com.hstudio.dto;

import java.util.List;

/** Regroupe les indicateurs du tableau de bord. */
public record DashboardResponse(
        long totalDemandes,
        long nouvellesDemandes,
        long aPrendreEnCharge,
        long enCoursTraitement,
        long propositionsEnvoyees,
        long gagnees,
        long perdues,
        List<CommercialKpi> kpisParCommercial
) {
}
