package com.likelion.dev_community.domain.subscription.dto;

import com.likelion.dev_community.domain.subscription.entity.PlanType;
import com.likelion.dev_community.domain.subscription.entity.Subscription;
import com.likelion.dev_community.domain.subscription.entity.SubscriptionStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscriptionResponse {
    private final PlanType planType;

    private final LocalDateTime startedAt;

    private final LocalDateTime expiresAt;

    private final SubscriptionStatus status;

    public static SubscriptionResponse from(Subscription subscription){
        return new SubscriptionResponse(
                subscription.getPlanType(),
                subscription.getStartedAt(),
                subscription.getExpiresAt(),
                subscription.getStatus()
        );
    }
}
