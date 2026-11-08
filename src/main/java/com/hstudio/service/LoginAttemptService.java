package com.hstudio.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hstudio.model.User;
import com.hstudio.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/** G les é de connexion, les blocages et la r par code. */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final int OTP_BOUND = 1_000_000;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.security.login.max-failed-attempts:3}")
    private int maxFailedAttempts;

    @Value("${app.security.login.lock-duration:15m}")
    private Duration lockDuration;

    @Value("${app.security.login.otp-duration:10m}")
    private Duration otpDuration;

    @Value("${app.security.login.otp-max-attempts:5}")
    private int maxOtpAttempts;

    @Value("${app.security.login.otp-resend-cooldown:1m}")
    private Duration otpResendCooldown;

    /** Enregistre é et applique le blocage ou la demande de r. */
    @Transactional
    public Optional<IssuedOtp> recordFailedAttempt(String email) {
        return userRepository.findByEmailForUpdate(normalize(email)).flatMap(user -> {
            Instant now = Instant.now();
            if (user.isPasswordResetRequired()
                    || (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now))) {
                return Optional.empty();
            }

            user.setLockedUntil(null);
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() < maxFailedAttempts) {
                return Optional.empty();
            }

            user.setFailedLoginAttempts(0);
            user.setLoginLockouts(user.getLoginLockouts() + 1);
            if (user.getLoginLockouts() == 1) {
                user.setLockedUntil(now.plus(lockDuration));
                return Optional.empty();
            }

            user.setPasswordResetRequired(true);
            user.setLockedUntil(null);
            return Optional.of(issueOtp(user, now));
        });
    }

    /** Efface le compteur é apres une connexion reussie. */
    @Transactional
    public void recordSuccessfulLogin(String email) {
        userRepository.findByEmailForUpdate(normalize(email)).ifPresent(user -> {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
        });
    }

    /** Emet un nouveau code en respectant le delai entre envois. */
    @Transactional
    public Optional<IssuedOtp> resendOtp(String email) {
        return userRepository.findByEmailForUpdate(normalize(email))
                .filter(User::isPasswordResetRequired)
                .flatMap(user -> {
                    Instant now = Instant.now();
                    if (user.getPasswordResetOtpSentAt() != null
                            && user.getPasswordResetOtpSentAt().plus(otpResendCooldown).isAfter(now)) {
                        return Optional.empty();
                    }
                    return Optional.of(issueOtp(user, now));
                });
    }

    /** V le code puis transmet le nouveau mot de passe. */
    @Transactional
    public boolean completePasswordReset(String email, String code, String newPassword) {
        Optional<User> optionalUser = userRepository.findByEmailForUpdate(normalize(email));
        if (optionalUser.isEmpty()) {
            return false;
        }

        User user = optionalUser.get();
        Instant now = Instant.now();
        if (!user.isPasswordResetRequired()
                || user.getPasswordResetOtpHash() == null
                || user.getPasswordResetOtpExpiresAt() == null
                || !user.getPasswordResetOtpExpiresAt().isAfter(now)
                || user.getPasswordResetOtpAttempts() >= maxOtpAttempts) {
            clearOtp(user);
            return false;
        }

        if (!passwordEncoder.matches(code, user.getPasswordResetOtpHash())) {
            user.setPasswordResetOtpAttempts(user.getPasswordResetOtpAttempts() + 1);
            if (user.getPasswordResetOtpAttempts() >= maxOtpAttempts) {
                clearOtp(user);
            }
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLoginLockouts(0);
        user.setLockedUntil(null);
        user.setPasswordResetRequired(false);
        clearOtp(user);
        return true;
    }

    /** G un code temporaire, enregistre son empreinte et son expiration. */
    private IssuedOtp issueOtp(User user, Instant now) {
        String code = "%06d".formatted(secureRandom.nextInt(OTP_BOUND));
        user.setPasswordResetOtpHash(passwordEncoder.encode(code));
        user.setPasswordResetOtpExpiresAt(now.plus(otpDuration));
        user.setPasswordResetOtpSentAt(now);
        user.setPasswordResetOtpAttempts(0);
        return new IssuedOtp(user.getEmail(), user.getFirstName(), code);
    }

    /** Efface le code de r et son compteur essais. */
    private void clearOtp(User user) {
        user.setPasswordResetOtpHash(null);
        user.setPasswordResetOtpExpiresAt(null);
        user.setPasswordResetOtpSentAt(null);
        user.setPasswordResetOtpAttempts(0);
    }

    /** Uniformise adresse e-mail pour retrouver le meme compte quelle que soit sa casse. */
    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** Regroupe le destinataire et le code de r emis. */
    public record IssuedOtp(String email, String firstName, String code) {
    }

}
