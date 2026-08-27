package com.likelion.dev_community.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieProvider {

    // 리프레시 토큰 쿠키 이름. 발급(AuthService, OAuth2SuccessHandler)과 만료(UserService),
    // 그리고 재발급 시 읽는 쪽(AuthController)이 같은 값을 써야 하므로 한 곳에 둔다.
    public static final String REFRESH_TOKEN = "refreshToken";

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    public ResponseCookie createCookie(String name, String value, Duration expiration) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(expiration)
                .build();
    }

    public ResponseCookie clearCookie(String name) {
        return createCookie(name, null, Duration.ZERO);
    }
}
