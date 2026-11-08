package com.hstudio.model;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**Un compte utilisateur et ses informations*/
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Size(min = 8, max = 72, message = "Le mot de passe doit contenir entre 8 et 72 caractères")
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true; // Pour l'affectation automatique parmi les commerciaux actifs

    @JsonIgnore
    @Builder.Default
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int failedLoginAttempts = 0;

    @JsonIgnore
    @Builder.Default
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int loginLockouts = 0;

    @JsonIgnore
    private Instant lockedUntil;

    @JsonIgnore
    @Builder.Default
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean passwordResetRequired = false;

    @JsonIgnore
    @Column(length = 60)
    private String passwordResetOtpHash;

    @JsonIgnore
    private Instant passwordResetOtpExpiresAt;

    @JsonIgnore
    private Instant passwordResetOtpSentAt;

    @JsonIgnore
    @Builder.Default
    @Column(nullable = false, columnDefinition = "integer default 0")
    private int passwordResetOtpAttempts = 0;
}