package com.likelion.dev_community.domain.user.dto.authDto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class OAuthLoginResponse {
    private final boolean registered;
    private final TokenResponse token;
    private final String signupToken;
    private final String suggestedNickname;

    public static OAuthLoginResponse ofExistingUser(TokenResponse token) {
        return new OAuthLoginResponse(true, token, null, null);
    }

    public static OAuthLoginResponse ofNewUser(String signupToken, String suggestedNickname) {
        return new OAuthLoginResponse(false, null, signupToken, suggestedNickname);
    }
}