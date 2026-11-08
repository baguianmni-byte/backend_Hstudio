package com.hstudio.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.hstudio.model.Role;
import com.hstudio.model.User;
import com.hstudio.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** V les blocages de connexion et la r securisee. */
class LoginAttemptServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final LoginAttemptService service = new LoginAttemptService(userRepository, passwordEncoder);
    private final User user = User.builder()
            .email("jean@example.com")
            .firstName("Jean")
            .lastName("Dupont")
            .password(passwordEncoder.encode("old-password"))
            .role(Role.Commercial)
            .build();

    /** Prepare les delais et donn de compte utilises par les tests. */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "maxFailedAttempts", 2);
        ReflectionTestUtils.setField(service, "lockDuration", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(service, "otpDuration", Duration.ofMinutes(10));
        ReflectionTestUtils.setField(service, "maxOtpAttempts", 2);
        ReflectionTestUtils.setField(service, "otpResendCooldown", Duration.ofMinutes(1));
        when(userRepository.findByEmailForUpdate("jean@example.com")).thenReturn(Optional.of(user));
    }

    /** V qu un second blocage declenche un code et impose la r. */
    @Test
    void issuesOtpOnSecondLockoutAndRequiresPasswordReset() {
        // Le premier seuil déclenche seulement un verrouillage temporaire.
        service.recordFailedAttempt("JEAN@example.com");
        service.recordFailedAttempt("jean@example.com");

        assertThat(user.getLoginLockouts()).isEqualTo(1);
        assertThat(user.getLockedUntil()).isAfter(Instant.now());
        assertThat(user.isPasswordResetRequired()).isFalse();

        // Après expiration, un nouveau cycle de tentatives atteint le seuil de réinitialisation.
        user.setLockedUntil(Instant.now().minusSeconds(1));
        service.recordFailedAttempt("jean@example.com");
        Optional<LoginAttemptService.IssuedOtp> issuedOtp =
                service.recordFailedAttempt("jean@example.com");

        assertThat(issuedOtp).isPresent();
        assertThat(user.getLoginLockouts()).isEqualTo(2);
        assertThat(user.isPasswordResetRequired()).isTrue();
        assertThat(user.getPasswordResetOtpHash()).isNotEqualTo(issuedOtp.orElseThrow().code());
        assertThat(passwordEncoder.matches(
                issuedOtp.orElseThrow().code(),
                user.getPasswordResetOtpHash()
        )).isTrue();
    }

    /** V qu un code valide change le mot de passe et retire le blocage. */
    @Test
    void resetsPasswordAndUnlocksOnlyWithValidOtp() {
        user.setPasswordResetRequired(true);
        user.setLoginLockouts(2);
        user.setPasswordResetOtpHash(passwordEncoder.encode("012345"));
        user.setPasswordResetOtpExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));

        // Le code erroné ne change pas le mot de passe; le code valide termine la réinitialisation.
        assertThat(service.completePasswordReset("jean@example.com", "999999", "new-password")).isFalse();
        assertThat(user.getPasswordResetOtpAttempts()).isEqualTo(1);

        assertThat(service.completePasswordReset("jean@example.com", "012345", "new-password")).isTrue();
        assertThat(passwordEncoder.matches("new-password", user.getPassword())).isTrue();
        assertThat(user.isPasswordResetRequired()).isFalse();
        assertThat(user.getLoginLockouts()).isZero();
        assertThat(user.getPasswordResetOtpHash()).isNull();
    }

    /** V que le code est supprime apres le maximum essais errones. */
    @Test
    void invalidatesOtpAfterConfiguredNumberOfFailures() {
        user.setPasswordResetRequired(true);
        user.setPasswordResetOtpHash(passwordEncoder.encode("012345"));
        user.setPasswordResetOtpExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));

        // Chaque code erroné consomme un essai; à la limite, le code devient inutilisable.
        assertThat(service.completePasswordReset("jean@example.com", "999999", "new-password")).isFalse();
        assertThat(service.completePasswordReset("jean@example.com", "999999", "new-password")).isFalse();

        assertThat(user.getPasswordResetOtpHash()).isNull();
        assertThat(user.isPasswordResetRequired()).isTrue();
    }
}
