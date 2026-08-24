package com.likelion.dev_community.domain.user.dto.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TokenResponse {
    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.abc123", description = "로그인 성공 시 발급되는 JWT 액세스 토큰. Authorization 헤더에 Bearer로 실어서 사용")
    private final String accessToken;
    @Schema(example = "Bearer ", description = "토큰 타입. 항상 Bearer 고정")
    private final String tokenType;

    public static TokenResponse of(String accessToken){
        return new TokenResponse(accessToken,"Bearer ");
    }
}
