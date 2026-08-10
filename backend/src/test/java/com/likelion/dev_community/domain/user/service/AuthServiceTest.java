package com.likelion.dev_community.domain.user.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.user.repository.RefreshTokenRepository;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import com.likelion.dev_community.security.jwt.CookieProvider;
import com.likelion.dev_community.security.jwt.JwtProvider;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private CookieProvider cookieProvider;

    // F-02-1: 만료된 리프레시 토큰은 401(UNAUTHORIZED)로 응답해야 한다
    @Test
    void 만료된_리프레시_토큰으로_재발급하면_401에_해당하는_예외가_발생한다() {
        AuthService authService = new AuthService(userRepository, passwordEncoder, jwtProvider, refreshTokenRepository, cookieProvider);

        when(jwtProvider.parseRefreshToken("expired-token"))
                .thenThrow(new ExpiredJwtException(null, null, "expired"));

        assertThatThrownBy(() -> authService.reissue("expired-token"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    ErrorCode errorCode = ((CustomException) e).getErrorCode();
                    assertThat(errorCode).isEqualTo(ErrorCode.EXPIRED_REFRESH_TOKEN);
                    assertThat(errorCode.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
    }
}
