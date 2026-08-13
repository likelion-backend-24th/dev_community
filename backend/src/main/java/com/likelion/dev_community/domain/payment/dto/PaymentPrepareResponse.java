package com.likelion.dev_community.domain.payment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentPrepareResponse {
    private final String storeId;

    private final String channelKey;

    private final String paymentId;

    private final String orderName;

    private final Long amount;

    private final String currency;

    private final List<ProductInfo> products;

    public static PaymentPrepareResponse of(String storeId, String channelKey, String paymentId,
                                            String orderName, Long amount, String currency, List<ProductInfo> products) {
        return new PaymentPrepareResponse(storeId, channelKey, paymentId, orderName, amount, currency, products);
    }
}
