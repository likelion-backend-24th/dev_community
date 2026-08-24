package com.likelion.dev_community.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentCancelRequest {
    @NotBlank(message = "취소 사유를 입력해주세요.")
    @Schema(example = "단순 변심", description = "결제 취소 사유")
    private final String reason;
}
