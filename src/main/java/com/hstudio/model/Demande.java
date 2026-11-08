package com.hstudio.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** R un dossier commercial soumis par un client et son suivi. */
@Entity
@Table(name = "demandes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // Identifiant unique de la demande

    // Informations de l'entreprise et du contact[cite: 1]
    @Column(nullable = false)
    private String companyName;       // Nom de l’entreprise
    private String industry;          // Secteur d’activité
    private String companySize;       // Taille de l’entreprise
    @Column(nullable = false)
    private String contactName;       // Nom du contact
    @Column(nullable = false)
    private String phone;             // Téléphone
    private String email;             // E-mail
    private String locality;          // Localité

    // Service demandé[cite: 1]
    @Column(nullable = false)
    private String servicePole;       // Ex: "Gestion de stock et Auto-caisse" ou "Données, IA et Automatisation"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String needDescription;   // Besoin ou problème à résoudre

    private String desiredDeadline;   // Délai souhaité
    private String estimatedBudget;   // Budget estimatif (facultatif)

    // Pipeline et Suivi CRM[cite: 1]
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DemandeStatus status = DemandeStatus.NOUVELLE_DEMANDE;

    @ManyToOne
    @JoinColumn(name = "commercial_id")
    private User assignedCommercial;  // Commercial responsable de l'affectation automatique ou manuelle

    private LocalDateTime assignedAt; // Date et heure de l'affectation[cite: 1]
    
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** Renseigne automatiquement la date de cr avant enregistrement. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = DemandeStatus.NOUVELLE_DEMANDE;
    }

    /** Actualise automatiquement la date de derni modification. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}