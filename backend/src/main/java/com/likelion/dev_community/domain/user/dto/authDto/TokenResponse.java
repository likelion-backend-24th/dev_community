package com.likelion.dev_community.domain.user.dto.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TokenResponse {
    @Schema(example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.abc123")
    private final String accessToken;
    @Schema(example = "Bearer ")
    private final String tokenType;

    public static TokenResponse of(String accessToken){
        return new TokenResponse(accessToken,"Bearer ");
    }
}
