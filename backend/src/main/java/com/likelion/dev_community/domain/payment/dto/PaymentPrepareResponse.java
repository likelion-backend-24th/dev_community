package com.likelion.dev_community.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Deprecated
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentPrepareResponse {
    @Schema(example = "store-ae356798-3d20-4969-b739-14c6b0e1a667")
    private final String storeId;

    @Schema(example = "channel-key-xxxx")
    private final String channelKey;

    @Schema(example = "DEV_3f2504e0-4f89-11d3-9a0c-0305e82c3301")
    private final String paymentId;

    @Schema(example = "프리미엄 구독")
    private final String orderName;

    @Schema(example = "4900")
    private final Long amount;

    @Schema(example = "KRW")
    private final String currency;

    private final List<ProductInfo> products;

    public static PaymentPrepareResponse of(String storeId, String channelKey, String paymentId,
                                            String orderName, Long amount, String currency, List<ProductInfo> products) {
        return new PaymentPrepareResponse(storeId, channelKey, paymentId, orderName, amount, currency, products);
    }
}
