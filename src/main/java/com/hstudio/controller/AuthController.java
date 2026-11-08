package com.hstudio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hstudio.dto.AuthResponse;
import com.hstudio.dto.LoginRequest;
import com.hstudio.dto.PasswordResetCompleteRequest;
import com.hstudio.dto.PasswordResetResendRequest;
import com.hstudio.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Expose la connexion et la r du mot de passe. */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    /** V les identifiants et renvoie le resultat de la connexion. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    /** Demande un nouvel envoi du code selon les regles de delai. */
    @PostMapping("/password-reset/resend")
    public ResponseEntity<Void> resendPasswordResetCode(
            @Valid @RequestBody PasswordResetResendRequest request
    ) {
        authenticationService.resendPasswordResetCode(request.email());
        return ResponseEntity.accepted().build();
    }

    /** V le code puis transmet le nouveau mot de passe. */
    @PostMapping("/password-reset/complete")
    public ResponseEntity<Void> completePasswordReset(
            @Valid @RequestBody PasswordResetCompleteRequest request
    ) {
        authenticationService.completePasswordReset(request);
        return ResponseEntity.noContent().build();
    }
}