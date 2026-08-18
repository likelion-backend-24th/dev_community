package com.likelion.dev_community.domain.payment.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BillingKeyPrepareResponse {
    private final String storeId;

    private final String channelKey;

    private final String issueId;

    public static BillingKeyPrepareResponse of(String storeId, String channelKey, String issueId) {
        return new BillingKeyPrepareResponse(storeId, channelKey, issueId);
    }
}
