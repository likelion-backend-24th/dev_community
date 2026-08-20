package com.likelion.dev_community.domain.notification.dto;

import com.likelion.dev_community.domain.notification.entity.Notification;
import com.likelion.dev_community.domain.notification.entity.NotificationType;

public record NotificationPayload(
        Long id,
        NotificationType type,
        String message,
        String link
) {
    public static NotificationPayload from(Notification notification) {
        return new NotificationPayload(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getLink()
        );
    }
}
