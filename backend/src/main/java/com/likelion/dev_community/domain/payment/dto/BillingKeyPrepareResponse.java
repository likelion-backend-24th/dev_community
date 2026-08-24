package com.likelion.dev_community.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BillingKeyPrepareResponse {
    @Schema(example = "store-ae356798-3d20-4969-b739-14c6b0e1a667")
    private final String storeId;

    @Schema(example = "channel-key-xxxx")
    private final String channelKey;

    @Schema(example = "ISSUE_3f2504e0-4f89-11d3-9a0c-0305e82c3301")
    private final String issueId;

    public static BillingKeyPrepareResponse of(String storeId, String channelKey, String issueId) {
        return new BillingKeyPrepareResponse(storeId, channelKey, issueId);
    }
}
