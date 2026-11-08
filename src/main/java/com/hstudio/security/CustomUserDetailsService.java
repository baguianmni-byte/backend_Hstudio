package com.hstudio.security;

import java.time.Instant;
import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hstudio.model.User;
import com.hstudio.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/** Charge un compte et ses droits pour la s Spring. */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /** Charge utilisateur et ses roles a partir de son nom de connexion. */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'e-mail : " + email));

        // Associe le rôle de l'utilisateur (ex: ROLE_COMMERCIAL, ROLE_RESPONSABLE, ROLE_ADMIN)
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().name());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                true,
                true,
                !user.isPasswordResetRequired()
                        && (user.getLockedUntil() == null || !user.getLockedUntil().isAfter(Instant.now())),
                Collections.singletonList(authority)
        );
    }
}