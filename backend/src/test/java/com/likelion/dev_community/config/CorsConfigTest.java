package com.likelion.dev_community.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    // cors.additional-origins 프로퍼티가 없을 때(기본값 "") 기존 origin 5개만 반환되는지 확인한다.
    // 즉, 이 프로퍼티를 새로 도입하기 전(main)과 동일한 CORS 동작이어야 한다.
    @Test
    void getAllowedOrigins_추가_origin_미설정시_기존_목록만_반환한다() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "additionalOrigins", "");

        assertThat(corsConfig.getAllowedOrigins()).containsExactly(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "https://dev-com.duckdns.org"
        );
    }

    @Test
    void getAllowedOrigins_추가_origin을_콤마로_구분해_병합한다() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(
                corsConfig, "additionalOrigins", "https://dev-community.vercel.app, https://dev-community-git-main.vercel.app");

        assertThat(corsConfig.getAllowedOrigins())
                .contains("https://dev-com.duckdns.org")
                .contains("https://dev-community.vercel.app")
                .contains("https://dev-community-git-main.vercel.app");
    }

    @Test
    void getAllowedOrigins_빈_문자열이나_공백_토큰은_무시한다() {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "additionalOrigins", "https://dev-community.vercel.app,, ,");

        assertThat(corsConfig.getAllowedOrigins())
                .hasSize(6)
                .contains("https://dev-community.vercel.app");
    }
}
