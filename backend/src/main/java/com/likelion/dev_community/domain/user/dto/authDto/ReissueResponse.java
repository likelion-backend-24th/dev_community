package com.likelion.dev_community.domain.user.dto.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReissueResponse {
    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.abc123", description = "리프레시 토큰으로 새로 발급된 JWT 액세스 토큰")
    private String accessToken;

    public static ReissueResponse of(String accessToken){
        return new ReissueResponse(accessToken);
    }
}
