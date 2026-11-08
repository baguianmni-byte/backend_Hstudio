package com.hstudio.service;

import com.hstudio.exception.ResourceNotFoundException;
import com.hstudio.model.Notification;
import com.hstudio.model.User;
import com.hstudio.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Cr et consulte les notifications et met a jour leur etat. */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /** Enregistre une notification pour son destinataire. */
    @Transactional
    public void creerNotification(User recipient, String title, String message, UUID demandeId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setDemandeId(demandeId);
        notificationRepository.save(notification);
    }

    /** Renvoie les notifications de utilisateur. */
    public List<Notification> obtenirNotificationsUtilisateur(User user) {
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    /** Marque comme lue une notification du compte connect. */
    @Transactional
    public Notification marquerCommeLue(UUID notificationId, User recipient) {
        Notification notification = notificationRepository.findByIdAndRecipient(notificationId, recipient)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable avec l'ID : " + notificationId));
        notification.setReadStatus(true);
        return notificationRepository.save(notification);
    }
}