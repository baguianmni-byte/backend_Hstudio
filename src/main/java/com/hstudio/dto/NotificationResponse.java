package com.hstudio.dto;

import com.hstudio.model.Notification;

import java.time.LocalDateTime;
import java.util.UUID;
/** Presente une notification destin a un utilisateur. */
public record NotificationResponse(
        UUID id,
        UserSummary recipient,
        String title,
        String message,
        boolean readStatus,
        UUID demandeId,
        LocalDateTime createdAt
) {
    /** Convertit les donn metier en objet de r API. */
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                UserSummary.from(notification.getRecipient()),
                notification.getTitle(),
                notification.getMessage(),
                notification.isReadStatus(),
                notification.getDemandeId(),
                notification.getCreatedAt()
        );
    }
}
