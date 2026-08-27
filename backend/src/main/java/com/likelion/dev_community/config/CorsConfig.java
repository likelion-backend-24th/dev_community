package com.likelion.dev_community.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class CorsConfig {

    // WebSocketConfig의 STOMP endpoint도 같은 origin 목록을 쓰므로 getAllowedOrigins()로 재사용.
    // 평문 HTTP EC2 IP(http://13.124.217.2)는 자격 증명 포함 요청이 암호화 없이 오갈 수
    // 있어 제거함 — 운영 접속은 반드시 https://dev-com.duckdns.org로만 이뤄져야 한다.
    private static final List<String> BASE_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "https://dev-com.duckdns.org"
    );

    // 프론트를 Vercel 등 별도 도메인으로 옮길 때 코드 변경 없이 이 프로퍼티(콤마 구분)로
    // origin을 추가한다. 기본값이 빈 문자열이라 미설정 시 기존 동작과 동일하다.
    @Value("${cors.additional-origins:}")
    private String additionalOrigins;

    private List<String> allowedOrigins;

    public List<String> getAllowedOrigins() {
        if (allowedOrigins == null) {
            List<String> combined = new ArrayList<>(BASE_ORIGINS);
            if (additionalOrigins != null && !additionalOrigins.isBlank()) {
                Arrays.stream(additionalOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isEmpty())
                        .forEach(combined::add);
            }
            allowedOrigins = Collections.unmodifiableList(combined);
        }
        return allowedOrigins;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(getAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}