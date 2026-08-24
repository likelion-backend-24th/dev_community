package com.likelion.dev_community.domain.payment.dto;

import com.likelion.dev_community.domain.payment.entity.payment.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCancelResponse {
    @Schema(example = "DEV_3f2504e0-4f89-11d3-9a0c-0305e82c3301")
    private final String paymentId;

    private final PaymentStatus status;

    @Schema(example = "2026-08-23T11:00:00")
    private final LocalDateTime cancelledAt;

    @Schema(example = "단순 변심")
    private final String reason;

    public static PaymentCancelResponse of(String paymentId, PaymentStatus status, LocalDateTime cancelledAt, String reason) {
        return new PaymentCancelResponse(paymentId, status, cancelledAt, reason);
    }
}
