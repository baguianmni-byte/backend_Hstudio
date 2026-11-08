package com.hstudio.repository;

import com.hstudio.model.Role;
import com.hstudio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Fournit les recherches en base pour les comptes utilisateurs. */
public interface UserRepository extends JpaRepository<User, UUID> {
    /** Recherche un compte a partir de son adresse e-mail. */
    Optional<User> findByEmail(String email); // Recherche un utilisateur par adresse e-mail

    /** Verrouille le compte trouve pendant sa mise a jour. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.email = :email")
    Optional<User> findByEmailForUpdate(@Param("email") String email);

    /** Recherche les utilisateurs qui possèdent le rôle indiqué. */
    List<User> findByRole(Role role);
    /** Recherche les comptes actifs ayant le role indique. */
    List<User> findByRoleAndActiveTrue(Role role); // Recherche les utilisateurs actifs ayant ce rôle
    /** V si adresse existe sans distinguer sa casse. */
    boolean existsByEmailIgnoreCase(String email);
}