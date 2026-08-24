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
    private String email;

    public OAuthSignupInfo(String signupToken, String provider, String providerId, String email) {
        this.signupToken = signupToken;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
    }
}