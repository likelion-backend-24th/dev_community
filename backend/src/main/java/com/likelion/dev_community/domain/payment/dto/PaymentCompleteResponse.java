package com.likelion.dev_community.domain.payment.dto;

import com.likelion.dev_community.domain.payment.entity.payment.PaymentStatus;
import com.likelion.dev_community.domain.subscription.entity.PlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCompleteResponse {
    @Schema(example = "DEV_3f2504e0-4f89-11d3-9a0c-0305e82c3301", description = "결제 건 고유 ID")
    private final String paymentId;

    private final PlanType planType;

    private final PaymentStatus status;

    @Schema(example = "2026-08-23T10:00:00", description = "결제 완료 일시")
    private final LocalDateTime paidAt;

    public static PaymentCompleteResponse of(String paymentId, PlanType planType, PaymentStatus status, LocalDateTime paidAt) {
        return new PaymentCompleteResponse(paymentId, planType, status, paidAt);
    }
}
