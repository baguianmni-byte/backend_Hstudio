package com.hstudio.dto;

import java.util.UUID;

/** Regroupe les indicateurs activit un commercial. */
public record CommercialKpi(
        UUID commercialId,
        String commercialName,
        long totalDemandes,
        long demandesActives,
        long demandesGagnees,
        long demandesPerdues
) {
}
