package com.likelion.dev_community.domain.notification.dto;

import com.likelion.dev_community.domain.notification.entity.Notification;
import com.likelion.dev_community.domain.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record NotificationPayload(
        @Schema(example = "1", description = "알림 ID")
        Long id,
        NotificationType type,
        @Schema(example = "회원님의 질문에 새 답변이 달렸습니다.", description = "알림 메시지")
        String message,
        @Schema(example = "/questions/10", description = "알림 클릭 시 이동할 프론트엔드 경로")
        String link,
        @Schema(example = "false", description = "읽음 여부")
        boolean isRead,
        @Schema(example = "2026-08-23T10:00:00", description = "알림 발생일시")
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
