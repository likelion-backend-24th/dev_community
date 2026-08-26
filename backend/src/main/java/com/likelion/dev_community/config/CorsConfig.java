package com.likelion.dev_community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    // WebSocketConfig의 STOMP endpoint도 같은 origin 목록을 쓰므로 패키지 내에서 재사용
    // 평문 HTTP EC2 IP(http://13.124.217.2)는 자격 증명 포함 요청이 암호화 없이 오갈 수
    // 있어 제거함 — 운영 접속은 반드시 https://dev-com.duckdns.org로만 이뤄져야 한다.
    static final List<String> ALLOWED_ORIGINS = List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:5500",
            "http://127.0.0.1:5500",
            "https://dev-com.duckdns.org"
    );

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}