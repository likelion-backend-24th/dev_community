package com.likelion.dev_community.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Deprecated
@Getter
@AllArgsConstructor
public class ProductInfo {
    @Schema(example = "PREMIUM", description = "상품 ID")
    private final String id;

    @Schema(example = "프리미엄 구독", description = "상품명")
    private final String name;

    @Schema(example = "PREMIUM", description = "상품 코드")
    private final String code;

    @Schema(example = "4900", description = "상품 단가(원)")
    private final Long amount;

    @Schema(example = "1", description = "수량")
    private final int quantity;
}
