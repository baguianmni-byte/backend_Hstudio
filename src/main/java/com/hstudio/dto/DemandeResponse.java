package com.hstudio.dto;

import com.hstudio.model.Demande;
import com.hstudio.model.DemandeStatus;

import java.time.LocalDateTime;
import java.util.UUID;
/** Presente les informations une demande dans les r API. */
public record DemandeResponse(
        UUID id,
        String companyName,
        String industry,
        String companySize,
        String contactName,
        String phone,
        String email,
        String locality,
        String servicePole,
        String needDescription,
        String desiredDeadline,
        String estimatedBudget,
        DemandeStatus status,
        UserSummary assignedCommercial,
        LocalDateTime assignedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** Convertit les donn metier en objet de r API. */
    public static DemandeResponse from(Demande demande) {
        return new DemandeResponse(
                demande.getId(),
                demande.getCompanyName(),
                demande.getIndustry(),
                demande.getCompanySize(),
                demande.getContactName(),
                demande.getPhone(),
                demande.getEmail(),
                demande.getLocality(),
                demande.getServicePole(),
                demande.getNeedDescription(),
                demande.getDesiredDeadline(),
                demande.getEstimatedBudget(),
                demande.getStatus(),
                UserSummary.from(demande.getAssignedCommercial()),
                demande.getAssignedAt(),
                demande.getCreatedAt(),
                demande.getUpdatedAt()
        );
    }
}
