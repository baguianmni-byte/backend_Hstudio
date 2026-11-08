package com.hstudio.service;

import com.hstudio.dto.UserCreateRequest;
import com.hstudio.exception.EmailAlreadyExistsException;
import com.hstudio.model.Role;
import com.hstudio.model.User;
import com.hstudio.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** V la cr des comptes et le rejet des courriels deja utilises. */
class UserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserService userService = new UserService(userRepository, passwordEncoder);

    /** V que la cr normalise le courriel, active le compte et protege le mot de passe. */
    @Test
    void createsActiveUserWithNormalizedEmailAndEncodedPassword() {
        when(userRepository.existsByEmailIgnoreCase("jean@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.createUser(new UserCreateRequest(
                " Jean ",
                " Dupont ",
                " JEAN@EXAMPLE.COM ",
                "temporary-password",
                Role.Commercial
        ));

        assertThat(createdUser.getEmail()).isEqualTo("jean@example.com");
        assertThat(createdUser.getFirstName()).isEqualTo("Jean");
        assertThat(createdUser.getLastName()).isEqualTo("Dupont");
        assertThat(createdUser.getPassword()).isNotEqualTo("temporary-password");
        assertThat(passwordEncoder.matches("temporary-password", createdUser.getPassword())).isTrue();
        assertThat(createdUser.isActive()).isTrue();
        assertThat(createdUser.getRole()).isEqualTo(Role.Commercial);
        verify(userRepository).save(createdUser);
    }

    /** V qu un courriel deja utilise est rejete meme si sa casse differe. */
    @Test
    void rejectsEmailAlreadyUsedRegardlessOfCase() {
        when(userRepository.existsByEmailIgnoreCase("jean@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(new UserCreateRequest(
                "Jean",
                "Dupont",
                "JEAN@example.com",
                "temporary-password",
                Role.Commercial
        ))).isInstanceOf(EmailAlreadyExistsException.class);
    }
}
