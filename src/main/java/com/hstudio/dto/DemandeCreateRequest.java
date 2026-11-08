package com.hstudio.dto;

import com.hstudio.model.Demande;
import jakarta.validation.constraints.NotBlank;
/** D les informations transmises pour cr une demande. */
public record DemandeCreateRequest(
        @NotBlank String companyName,
        String industry,
        String companySize,
        @NotBlank String contactName,
        String phone,
        String email,
        String locality,
        @NotBlank String servicePole,
        @NotBlank String needDescription,
        String desiredDeadline,
        String estimatedBudget
) {
    /** Convertit les donn re en objet metier. */
    public Demande toEntity() {
        Demande demande = new Demande();
        demande.setCompanyName(companyName);
        demande.setIndustry(industry);
        demande.setCompanySize(companySize);
        demande.setContactName(contactName);
        demande.setPhone(phone);
        demande.setEmail(email);
        demande.setLocality(locality);
        demande.setServicePole(servicePole);
        demande.setNeedDescription(needDescription);
        demande.setDesiredDeadline(desiredDeadline);
        demande.setEstimatedBudget(estimatedBudget);
        return demande;
    }
}
