package com.hstudio.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** R une action a effectuer sur une demande. */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande; // Dossier concerné[cite: 1]

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User assignedUser; // Commercial responsable

    @Column(nullable = false)
    private LocalDateTime dueDate; // Date et heure prévues[cite: 1]

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;    // Description de l'action à réaliser[cite: 1]

    @Column(nullable = false)
    @Builder.Default
    private boolean completed = false; // Statut de la tâche[cite: 1]

    private LocalDateTime createdAt;

    /** Renseigne automatiquement la date de cr avant enregistrement. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}