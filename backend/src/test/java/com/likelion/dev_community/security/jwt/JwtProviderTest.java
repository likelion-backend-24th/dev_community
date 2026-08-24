package com.likelion.dev_community.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    private static final String ACCESS_SECRET =
            Base64.getEncoder().encodeToString("access-secret-key-for-jwt-provider-test".getBytes());
    private static final String REFRESH_SECRET =
            Base64.getEncoder().encodeToString("refresh-secret-key-for-jwt-provider-test".getBytes());

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(ACCESS_SECRET, 60_000L, REFRESH_SECRET, 3_600_000L);
    }

    @Test
    void 액세스_토큰을_생성하고_클레임을_그대로_복원한다() {
        String token = jwtProvider.createAccessToken(1L, "leocho", "레오", List.of("USER"));

        Claims claims = jwtProvider.parseAccessToken(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("username", String.class)).isEqualTo("leocho");
        assertThat(claims.get("nickname", String.class)).isEqualTo("레오");
        assertThat(claims.get("roles", List.class)).containsExactly("USER");
    }

    @Test
    void 리프레시_토큰을_생성하고_subject를_복원한다() {
        String token = jwtProvider.createRefreshToken(42L);

        Claims claims = jwtProvider.parseRefreshToken(token);

        assertThat(claims.getSubject()).isEqualTo("42");
    }

    @Test
    void 만료된_액세스_토큰을_파싱하면_예외가_발생한다() {
        JwtProvider shortLivedProvider = new JwtProvider(ACCESS_SECRET, -1_000L, REFRESH_SECRET, 3_600_000L);
        String expiredToken = shortLivedProvider.createAccessToken(1L, "leocho", "레오", List.of("USER"));

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void 액세스_토큰을_리프레시_시크릿으로_검증하면_실패한다() {
        String accessToken = jwtProvider.createAccessToken(1L, "leocho", "레오", List.of("USER"));

        assertThatThrownBy(() -> jwtProvider.parseRefreshToken(accessToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void 리프레시_토큰을_액세스_시크릿으로_검증하면_실패한다() {
        String refreshToken = jwtProvider.createRefreshToken(1L);

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(refreshToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void 다른_시크릿으로_생성된_provider의_토큰은_검증에_실패한다() {
        String otherSecret = Base64.getEncoder().encodeToString("completely-different-secret-key-value".getBytes());
        JwtProvider otherProvider = new JwtProvider(otherSecret, 60_000L, REFRESH_SECRET, 3_600_000L);
        String token = otherProvider.createAccessToken(1L, "leocho", "레오", List.of("USER"));

        assertThatThrownBy(() -> jwtProvider.parseAccessToken(token))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void 리프레시_토큰_만료시간을_그대로_노출한다() {
        assertThat(jwtProvider.getRefreshTokenExpirationMs()).isEqualTo(3_600_000L);
    }
}
