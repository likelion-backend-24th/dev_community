package com.likelion.dev_community.domain.user.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "oauthSignupInfo", timeToLive = 1200) // 20분
public class OAuthSignupInfo {

    @Id
    private String signupToken;
    private String provider;
    private String providerId;

    public OAuthSignupInfo(String signupToken, String provider, String providerId) {
        this.signupToken = signupToken;
        this.provider = provider;
        this.providerId = providerId;
    }
}