package com.hstudio.service;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hstudio.dto.AuthResponse;
import com.hstudio.dto.LoginRequest;
import com.hstudio.dto.PasswordResetCompleteRequest;
import com.hstudio.model.User;
import com.hstudio.repository.UserRepository;
import com.hstudio.security.JwtService;

import lombok.RequiredArgsConstructor;

/** Coordonne la connexion et la r du mot de passe. */
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordResetEmailService passwordResetEmailService;

    /** V les identifiants et renvoie le resultat de la connexion. */
    public AuthResponse authenticate(LoginRequest request) {
        String email = normalize(request.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password())
            );
        } catch (BadCredentialsException exception) {
            loginAttemptService.recordFailedAttempt(email)
                    .ifPresent(otp -> passwordResetEmailService.sendCode(otp.email(), otp.firstName(), otp.code()));
            throw exception;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification invalide."));
        loginAttemptService.recordSuccessfulLogin(email);

        UserDetails principal = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().name())
                .build();
        String token = jwtService.generateToken(principal);
        return new AuthResponse(token, user.getEmail(), user.getRole(), user.getFirstName(), user.getLastName());
    }

    /** Demande un nouvel envoi du code selon les regles de delai. */
    public void resendPasswordResetCode(String email) {
        loginAttemptService.resendOtp(email)
                .ifPresent(otp -> passwordResetEmailService.sendCode(otp.email(), otp.firstName(), otp.code()));
    }

    /** V le code puis transmet le nouveau mot de passe. */
    public void completePasswordReset(PasswordResetCompleteRequest request) {
        boolean updated = loginAttemptService.completePasswordReset(
                request.email(), request.code(), request.newPassword()
        );
        if (!updated) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code OTP invalide ou expiré.");
        }
    }

    /** Uniformise adresse e-mail pour retrouver le meme compte quelle que soit sa casse. */
    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
