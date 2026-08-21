package com.likelion.dev_community.domain.notification.entity;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import com.likelion.dev_community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String message;

    // 알림 클릭 시 이동할 프론트 라우트 경로 (예: /questions/12)
    @Column(nullable = false, length = 200)
    private String link;

    @Column(nullable = false)
    private boolean isRead;

    @Builder
    public Notification(User recipient, NotificationType type, String message, String link) {
        this.recipient = recipient;
        this.type = type;
        this.message = message;
        this.link = link;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
