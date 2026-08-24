package com.likelion.dev_community.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Deprecated
@Getter
@AllArgsConstructor
public class ProductInfo {
    @Schema(example = "PREMIUM")
    private final String id;

    @Schema(example = "프리미엄 구독")
    private final String name;

    @Schema(example = "PREMIUM")
    private final String code;

    @Schema(example = "4900")
    private final Long amount;

    @Schema(example = "1")
    private final int quantity;
}
