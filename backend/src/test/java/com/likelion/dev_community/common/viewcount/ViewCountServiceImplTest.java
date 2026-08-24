package com.likelion.dev_community.common.viewcount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewCountServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ViewCountServiceImpl viewCountService;

    @BeforeEach
    void setUp() {
        viewCountService = new ViewCountServiceImpl(redisTemplate);
    }

    @Test
    void 처음_조회한_뷰어면_조회수를_증가시켜야_한다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("view:10:127.0.0.1"), eq("1"), any(Duration.class)))
                .thenReturn(true);

        boolean result = viewCountService.shouldIncrease(10L, "127.0.0.1");

        assertThat(result).isTrue();
    }

    @Test
    void 이미_조회한_뷰어면_조회수를_증가시키지_않는다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("view:10:127.0.0.1"), eq("1"), any(Duration.class)))
                .thenReturn(false);

        boolean result = viewCountService.shouldIncrease(10L, "127.0.0.1");

        assertThat(result).isFalse();
    }

    @Test
    void redis_setIfAbsent이_null을_반환해도_증가시키지_않는다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("view:10:127.0.0.1"), eq("1"), any(Duration.class)))
                .thenReturn(null);

        boolean result = viewCountService.shouldIncrease(10L, "127.0.0.1");

        assertThat(result).isFalse();
    }

    @Test
    void redis_장애시_예외를_전파하지_않고_조회수_증가를_건너뛴다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("view:10:127.0.0.1"), eq("1"), any(Duration.class)))
                .thenThrow(new QueryTimeoutException("redis timeout"));

        boolean result = viewCountService.shouldIncrease(10L, "127.0.0.1");

        assertThat(result).isFalse();
    }

    @Test
    void 서로_다른_뷰어키는_서로_다른_redis_key로_구분된다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq("view:10:userA"), eq("1"), any(Duration.class)))
                .thenReturn(true);
        when(valueOperations.setIfAbsent(eq("view:10:userB"), eq("1"), any(Duration.class)))
                .thenReturn(true);

        assertThat(viewCountService.shouldIncrease(10L, "userA")).isTrue();
        assertThat(viewCountService.shouldIncrease(10L, "userB")).isTrue();
    }
}
