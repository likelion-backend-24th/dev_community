package com.likelion.dev_community.domain.payment.dto;

import com.likelion.dev_community.domain.payment.entity.payment.PaymentStatus;
import com.likelion.dev_community.domain.subscription.entity.PlanType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCompleteResponse {
    private final String paymentId;

    private final PlanType planType;

    private final PaymentStatus status;

    private final LocalDateTime paidAt;

    public static PaymentCompleteResponse of(String paymentId, PlanType planType, PaymentStatus status, LocalDateTime paidAt) {
        return new PaymentCompleteResponse(paymentId, planType, status, paidAt);
    }
}
