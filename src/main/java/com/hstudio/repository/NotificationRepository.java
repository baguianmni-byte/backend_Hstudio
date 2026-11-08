package com.hstudio.repository;

import com.hstudio.model.Notification;
import com.hstudio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/** Fournit les recherches en base pour les notifications. */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    /** Renvoie les notifications du destinataire, des plus récentes aux plus anciennes. */
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    /** Recherche les notifications non lues du destinataire indiqué. */
    List<Notification> findByRecipientAndReadStatusFalse(User recipient);
    /** Recherche une notification appartenant au destinataire indiqué. */
    java.util.Optional<Notification> findByIdAndRecipient(UUID id, User recipient);
}