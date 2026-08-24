package com.likelion.dev_community.domain.payment.dto;

import com.likelion.dev_community.domain.subscription.entity.PlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BillingKeyIssueRequest {
    @NotBlank(message = "빌링키가 필요합니다.")
    @Schema(example = "billing-key-75ae3cab-6afe-422d-bf34-3a7b1762451d")
    private final String billingKey;

    @NotNull(message = "구독 등급을 선택해주세요.")
    private final PlanType planType;
}
