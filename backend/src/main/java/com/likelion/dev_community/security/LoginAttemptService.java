package com.likelion.dev_community.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

// 아이디 기준 로그인 실패 횟수를 Redis에 기록해 브루트포스 시도를 막는다.
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String KEY_PREFIX = "login:fail:";
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final StringRedisTemplate redisTemplate;

    public boolean isLocked(String username) {
        try {
            String value = redisTemplate.opsForValue().get(KEY_PREFIX + username);
            return value != null && Integer.parseInt(value) >= MAX_ATTEMPTS;
        } catch (DataAccessException e) {
            log.warn("로그인 시도 횟수 조회 실패 - username={}, 잠금 확인을 건너뜁니다", username, e);
            return false;
        }
    }

    public void recordFailure(String username) {
        try {
            String key = KEY_PREFIX + username;
            Long attempts = redisTemplate.opsForValue().increment(key);
            if (attempts != null && attempts == 1L) {
                redisTemplate.expire(key, LOCK_DURATION);
            }
        } catch (DataAccessException e) {
            log.warn("로그인 실패 횟수 기록 실패 - username={}", username, e);
        }
    }

    public void reset(String username) {
        try {
            redisTemplate.delete(KEY_PREFIX + username);
        } catch (DataAccessException e) {
            log.warn("로그인 실패 횟수 초기화 실패 - username={}", username, e);
        }
    }
}
