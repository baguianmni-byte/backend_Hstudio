package com.hstudio.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/** Envoie le code de r par courriel. */
@Service
@RequiredArgsConstructor
public class PasswordResetEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.security.login.mail-from:}")
    private String mailFrom;

    @Value("${app.security.login.otp-duration:10m}")
    private Duration otpDuration;

    /** Compose puis envoie le code de réinitialisation à son destinataire. */
    public void sendCode(String email, String firstName, String code) {
        if (mailFrom.isBlank()) {
            throw new IllegalStateException("La configuration app.security.login.mail-from est requise pour envoyer un OTP.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(email);
        message.setSubject("Code de réinitialisation de votre mot de passe");
        message.setText("""
                Bonjour %s,

                Votre compte a été temporairement bloqué après plusieurs tentatives de connexion.
                Utilisez le code suivant pour définir un nouveau mot de passe : %s

                Ce code expire dans %d minutes et ne peut être utilisé qu'une seule fois.
                Si vous n'êtes pas à l'origine de cette demande, contactez l'administrateur.
                """.formatted(firstName, code, otpDuration.toMinutes()));
        mailSender.send(message);
    }
}
