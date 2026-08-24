package com.likelion.dev_community.domain.subscription.dto;

import com.likelion.dev_community.domain.subscription.entity.PlanType;
import com.likelion.dev_community.domain.subscription.entity.Subscription;
import com.likelion.dev_community.domain.subscription.entity.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscriptionResponse {
    private final PlanType planType;

    @Schema(example = "2026-08-11T10:00:00")
    private final LocalDateTime startedAt;

    @Schema(example = "2026-09-11T10:00:00")
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
