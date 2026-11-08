package com.hstudio.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hstudio.dto.NotificationResponse;
import com.hstudio.model.User;
import com.hstudio.service.NotificationService;
import com.hstudio.service.UserService;

import lombok.RequiredArgsConstructor;

/** Fournit les notifications et leur etat de lecture. */
@RestController
@RequestMapping("/crm/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    /** Renvoie les notifications du compte connect. */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> listerNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        User utilisateur = userService.findByEmail(userDetails.getUsername());
        
        List<NotificationResponse> notifications = notificationService.obtenirNotificationsUtilisateur(utilisateur).stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(notifications);
    }

    /** Marque comme lue une notification du compte connect. */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> marquerCommeLue(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User utilisateur = userService.findByEmail(userDetails.getUsername());
        NotificationResponse notification = NotificationResponse.from(
                notificationService.marquerCommeLue(id, utilisateur)
        );
        return ResponseEntity.ok(notification);
    }
}