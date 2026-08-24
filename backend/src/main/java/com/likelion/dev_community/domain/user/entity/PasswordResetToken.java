package com.likelion.dev_community.domain.user.entity;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@RedisHash(value = "passwordResetToken", timeToLive = 1800) // 30분
public class PasswordResetToken {

    @Id
    private String token;
    private Long userId;

    public PasswordResetToken(String token, Long userId) {
        this.token = token;
        this.userId = userId;
    }
}
