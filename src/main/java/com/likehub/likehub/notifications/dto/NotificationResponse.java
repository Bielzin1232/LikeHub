package com.likehub.likehub.notifications.dto;

import com.likehub.likehub.notifications.Notification;
import com.likehub.likehub.notifications.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String message,
        boolean read,
        Instant createdAt,
        UUID targetId,
        String senderUsername,
        String senderDisplayName
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt(),
                n.getTargetId(),
                n.getSender().getUsername(),
                n.getSender().getDisplayName()
        );
    }
}
