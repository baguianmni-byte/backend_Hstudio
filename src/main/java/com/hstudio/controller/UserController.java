package com.hstudio.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hstudio.dto.UserCreateRequest;
import com.hstudio.dto.UserResponse;
import com.hstudio.model.User;
import com.hstudio.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Expose la gestion des comptes utilisateurs. */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Renvoie les utilisateurs selon les droits du demandeur. */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('Admin', 'Directeur_General')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAllUsers().stream()
                .map(UserController::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    /** Renvoie les comptes ayant le role de commercial. */
    @GetMapping("/commerciaux")
    @PreAuthorize("hasAnyAuthority('Admin', 'Directeur_General', 'Directeur_Marketing')")
    public ResponseEntity<List<UserResponse>> getCommercials() {
        List<UserResponse> users = userService.findCommercials().stream()
                .map(UserController::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    /** Cr un compte a partir des informations re. */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('Admin', 'Directeur_General')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        User createdUser = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(createdUser));
    }

    /** Active ou desactive le compte indique. */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyAuthority('Admin', 'Directeur_General')")
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable UUID id) {
        User updatedUser = userService.toggleActiveStatus(id);
        return ResponseEntity.ok(toResponse(updatedUser));
    }

    /** Convertit un compte metier en r sans exposer de donn sensibles. */
    private static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName() + " " + user.getLastName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive()
        );
    }
}