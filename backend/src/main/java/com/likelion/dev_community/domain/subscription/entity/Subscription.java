package com.likelion.dev_community.domain.subscription.entity;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import com.likelion.dev_community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanType planType;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    private String billingKey; // 정기결제용 빌링키

    @Builder
    public Subscription(User user, PlanType planType, LocalDateTime startedAt, LocalDateTime expiresAt) {
        this.user = user;
        this.planType = planType;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.status = SubscriptionStatus.ACTIVE;
    }

    // 신규 구독 생성
    public static Subscription create(User user, PlanType planType, LocalDateTime startedAt, LocalDateTime expiresAt) {
        return Subscription.builder()
                .user(user)
                .planType(planType)
                .startedAt(startedAt)
                .expiresAt(expiresAt)
                .build();
    }

    // 기존 구독 갱신
    public void activate(PlanType planType, LocalDateTime startedAt, LocalDateTime expiresAt) {
        this.planType = planType;
        this.startedAt = startedAt;
        this.expiresAt = expiresAt;
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    public void registerBillingKey(String billingKey) {
        this.billingKey = billingKey;
    }

    public void clearBillingKey() {
        this.billingKey = null;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }
}
