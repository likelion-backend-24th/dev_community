package com.likelion.dev_community.domain.payment.dto;

import com.likelion.dev_community.domain.subscription.entity.PlanType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentPrepareRequest {
    @NotNull(message = "구독 등급을 선택해주세요.")
    private final PlanType planType;
}
