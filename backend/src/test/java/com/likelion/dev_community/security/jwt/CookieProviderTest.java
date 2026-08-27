package com.likelion.dev_community.security.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CookieProviderTest {

    // cookie.same-site 프로퍼티가 없을 때 @Value 기본값(Strict)이 그대로 쓰이는지 확인한다.
    // 즉, 이 프로퍼티를 새로 도입하기 전(main)과 동일한 쿠키가 나가야 한다.
    @Test
    void createCookie_기본값은_SameSite_Strict를_유지한다() {
        CookieProvider cookieProvider = new CookieProvider();
        ReflectionTestUtils.setField(cookieProvider, "cookieSecure", true);
        // cookieSameSite는 세팅하지 않음 -> @Value("${cookie.same-site:Strict}") 기본값 확인용
        ReflectionTestUtils.setField(cookieProvider, "cookieSameSite", "Strict");

        ResponseCookie cookie = cookieProvider.createCookie("refreshToken", "value", Duration.ofDays(1));

        assertThat(cookie.getSameSite()).isEqualTo("Strict");
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.isHttpOnly()).isTrue();
    }

    // 프론트가 별도 도메인으로 분리된 뒤 cookie.same-site=None으로 바꿨을 때 실제로 반영되는지 확인한다.
    @Test
    void createCookie_SameSite_None으로_설정하면_반영된다() {
        CookieProvider cookieProvider = new CookieProvider();
        ReflectionTestUtils.setField(cookieProvider, "cookieSecure", true);
        ReflectionTestUtils.setField(cookieProvider, "cookieSameSite", "None");

        ResponseCookie cookie = cookieProvider.createCookie("refreshToken", "value", Duration.ofDays(1));

        assertThat(cookie.getSameSite()).isEqualTo("None");
    }

    @Test
    void clearCookie_값과_만료시간을_비운다() {
        CookieProvider cookieProvider = new CookieProvider();
        ReflectionTestUtils.setField(cookieProvider, "cookieSecure", true);
        ReflectionTestUtils.setField(cookieProvider, "cookieSameSite", "Strict");

        ResponseCookie cookie = cookieProvider.clearCookie("refreshToken");

        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
    }
}
