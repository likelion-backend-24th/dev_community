package com.likelion.dev_community.domain.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BillingKeyPrepareResponse {
    @Schema(example = "store-ae356798-3d20-4969-b739-14c6b0e1a667", description = "PortOne 상점 ID")
    private final String storeId;

    @Schema(example = "channel-key-xxxx", description = "PortOne 결제 채널 키")
    private final String channelKey;

    @Schema(example = "ISSUE_3f2504e0-4f89-11d3-9a0c-0305e82c3301", description = "빌링키 발급 요청 고유 ID. 프론트가 PortOne SDK로 빌링키 발급창을 호출할 때 사용")
    private final String issueId;

    public static BillingKeyPrepareResponse of(String storeId, String channelKey, String issueId) {
        return new BillingKeyPrepareResponse(storeId, channelKey, issueId);
    }
}
