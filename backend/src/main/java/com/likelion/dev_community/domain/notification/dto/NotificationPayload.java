package com.likelion.dev_community.domain.notification.dto;

import com.likelion.dev_community.domain.notification.entity.Notification;
import com.likelion.dev_community.domain.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NotificationPayload(
        @Schema(example = "1")
        Long id,
        NotificationType type,
        @Schema(example = "회원님의 질문에 새 답변이 달렸습니다.")
        String message,
        @Schema(example = "/questions/10")
        String link,
        @Schema(example = "false")
        boolean isRead,
        @Schema(example = "2026-08-23T10:00:00")
        LocalDateTime createdAt
) {
    public static NotificationPayload from(Notification notification) {
        return new NotificationPayload(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getLink(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
