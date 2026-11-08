package com.hstudio.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/** R un message de suivi destine a un utilisateur. */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User recipient; // Destinataire de la notification

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean readStatus = false; // Distinguer lues et non lues (Partie 2.6.3)

    private UUID demandeId; // Lien optionnel vers la demande concernée

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** Renseigne automatiquement la date de cr avant enregistrement. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.readStatus = false;
    }
}