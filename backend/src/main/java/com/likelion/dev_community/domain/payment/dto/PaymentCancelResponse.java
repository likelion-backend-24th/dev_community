package com.likelion.dev_community.domain.payment.dto;

import com.likelion.dev_community.domain.payment.entity.payment.PaymentStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentCancelResponse {
    private final String paymentId;

    private final PaymentStatus status;

    private final LocalDateTime cancelledAt;

    private final String reason;

    public static PaymentCancelResponse of(String paymentId, PaymentStatus status, LocalDateTime cancelledAt, String reason) {
        return new PaymentCancelResponse(paymentId, status, cancelledAt, reason);
    }
}
