package com.hstudio.service;

import com.hstudio.dto.UserCreateRequest;
import com.hstudio.exception.EmailAlreadyExistsException;
import com.hstudio.exception.ResourceNotFoundException;
import com.hstudio.model.Role;
import com.hstudio.model.User;
import com.hstudio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** G les comptes utilisateurs. */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Renvoie les comptes utilisateurs. */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /** Renvoie les comptes ayant le role commercial. */
    public List<User> findCommercials() {
        return userRepository.findByRole(Role.Commercial);
    }

    /** Recherche un compte a partir de son adresse e-mail. */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));
    }

    /** Cr un compte a partir des informations re. */
    public User createUser(UserCreateRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException("Cette adresse e-mail est déjà utilisée.");
        }

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .active(true)
                .build();
        return userRepository.save(user);
    }

    /** Inverse etat actif du compte et enregistre la modification. */
    public User toggleActiveStatus(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));
        
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }
}
