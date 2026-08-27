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

    // 프론트와 백엔드가 같은 도메인일 때는 Strict로 충분하지만, 프론트를 Vercel 등
    // 별도 도메인으로 분리하면 cross-site 요청에 Strict 쿠키가 실리지 않아 로그인
    // 유지가 깨진다. 그때는 None으로 바꿔야 하는데, 스펙상 SameSite=None은 Secure
    // 속성이 필수라 반드시 cookie.secure=true(HTTPS)와 함께 적용해야 한다.
    // 기본값 Strict는 현재(동일 도메인) 배포 동작을 그대로 유지한다.
    @Value("${cookie.same-site:Strict}")
    private String cookieSameSite;

    public ResponseCookie createCookie(String name, String value, Duration expiration) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(expiration)
                .build();
    }

    public ResponseCookie clearCookie(String name) {
        return createCookie(name, null, Duration.ZERO);
    }
}
