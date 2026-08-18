package com.likelion.dev_community.domain.payment.entity.webhook;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "webhook_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebhookEvent extends BaseTimeEntity { // 웹훅 중복 방지, 처리 상태와 원문 기록

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String webhookId; // PortOne webhook-id 헤더값

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(length = 50)
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookEventStatus status;

    @Column(length = 2000)
    private String rawPayload;

    @Builder
    public WebhookEvent(String webhookId, String eventType, String paymentId, String rawPayload) {
        this.webhookId = webhookId;
        this.eventType = eventType;
        this.paymentId = paymentId;
        this.rawPayload = rawPayload;
        this.status = WebhookEventStatus.RECEIVED;
    }

    public static WebhookEvent receive(String webhookId, String eventType, String paymentId, String rawPayload) {
        return WebhookEvent.builder()
                .webhookId(webhookId)
                .eventType(eventType)
                .paymentId(paymentId)
                .rawPayload(rawPayload)
                .build();
    }

    public void markProcessed() {
        this.status = WebhookEventStatus.PROCESSED;
    }

    public void markIgnored() {
        this.status = WebhookEventStatus.IGNORED;
    }
}
