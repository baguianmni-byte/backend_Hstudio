package com.hstudio.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/** R un echange att a une demande. */
@Entity
@Table(name = "interactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "demande_id", nullable = false)
    private Demande demande; // Liée à la demande concernée[cite: 1]

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;     // Auteur de l'enregistrement[cite: 1]

    @Column(nullable = false)
    private String interactionType; // Appel, e-mail, réunion, visite ou autre[cite: 1]

    @Column(nullable = false)
    private LocalDateTime interactionDate; // Moment de l'interaction[cite: 1]

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;          // Résumé de la conversation[cite: 1]

    @Column(columnDefinition = "TEXT")
    private String clientDecision;   // Résultat ou décision du client[cite: 1]

    private String nextAction;       // Prochaine action à entreprendre[cite: 1]
    private LocalDateTime nextActionDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** Renseigne automatiquement la date de cr avant enregistrement. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.interactionDate == null) {
            this.interactionDate = LocalDateTime.now();
        }
    }
}