package com.likelion.dev_community.domain.user.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.user.dto.authDto.ReissueResponse;
import com.likelion.dev_community.domain.user.dto.authDto.SignInRequest;
import com.likelion.dev_community.domain.user.dto.authDto.SignUpRequest;
import com.likelion.dev_community.domain.user.dto.authDto.SignUpResponse;
import com.likelion.dev_community.domain.user.dto.authDto.TokenResponse;
import com.likelion.dev_community.domain.user.entity.RefreshToken;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import com.likelion.dev_community.domain.user.repository.RefreshTokenRepository;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import com.likelion.dev_community.security.jwt.CookieProvider;
import com.likelion.dev_community.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

    @Mock
    private Claims claims;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtProvider, refreshTokenRepository, cookieProvider);
    }

    // ===== signUp (F-01) =====

    @Test
    void 정상적으로_회원가입한다() {
        SignUpRequest request = new SignUpRequest("newuser", "password123", "newnick");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByNickname("newnick")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        SignUpResponse response = authService.signUp(request);

        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getNickname()).isEqualTo("newnick");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 아이디가_중복이면_회원가입에_실패한다() {
        SignUpRequest request = new SignUpRequest("dupuser", "password123", "newnick");

        when(userRepository.existsByUsername("dupuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE));
    }

    @Test
    void 닉네임이_중복이면_회원가입에_실패한다() {
        SignUpRequest request = new SignUpRequest("newuser", "password123", "dupnick");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByNickname("dupnick")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE));
    }

    // ===== signIn (F-02) =====

    @Test
    void 정상적으로_로그인하면_액세스_리프레시_토큰을_발급한다() {
        User user = createUser(1L, "tester", "encoded-password", UserStatus.ACTIVE);
        SignInRequest request = new SignInRequest("tester", "raw-password");
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);
        when(jwtProvider.createAccessToken(1L, "tester", "tester", List.of("USER"))).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(1L)).thenReturn("refresh-token");
        when(jwtProvider.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(cookieProvider.createCookie(eq("refreshToken"), eq("refresh-token"), any(Duration.class)))
                .thenReturn(ResponseCookie.from("refreshToken", "refresh-token").build());

        TokenResponse response = authService.signIn(request, httpServletResponse);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void 존재하지_않는_아이디로_로그인하면_INVALID_CREDENTIALS() {
        SignInRequest request = new SignInRequest("no-such-user", "raw-password");

        when(userRepository.findByUsername("no-such-user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signIn(request, new MockHttpServletResponse()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    // 존재하지 않는 계정과 동일한 에러코드(INVALID_CREDENTIALS)를 반환해야 계정 열거 공격을 막을 수 있다
    @Test
    void 비밀번호가_틀리면_INVALID_CREDENTIALS() {
        User user = createUser(1L, "tester", "encoded-password", UserStatus.ACTIVE);
        SignInRequest request = new SignInRequest("tester", "wrong-password");

        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.signIn(request, new MockHttpServletResponse()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));
    }

    @Test
    void 탈퇴한_계정은_로그인이_차단된다() {
        User user = createUser(1L, "withdrawn", "encoded-password", UserStatus.WITHDRAWN);
        SignInRequest request = new SignInRequest("withdrawn", "raw-password");

        when(userRepository.findByUsername("withdrawn")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.signIn(request, new MockHttpServletResponse()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.WITHDRAWN_ACCOUNT));
    }

    // ===== reissue (F-02-1) =====

    @Test
    void 정상적으로_토큰을_재발급한다() {
        User user = createUser(1L, "tester", "encoded-password", UserStatus.ACTIVE);
        RefreshToken savedToken = new RefreshToken(1L, "valid-refresh-token", 604800000L);

        when(claims.getSubject()).thenReturn("1");
        when(jwtProvider.parseRefreshToken("valid-refresh-token")).thenReturn(claims);
        when(refreshTokenRepository.findById(1L)).thenReturn(Optional.of(savedToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtProvider.createAccessToken(1L, "tester", "tester", List.of("USER"))).thenReturn("new-access-token");

        ReissueResponse response = authService.reissue("valid-refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    void 만료된_리프레시_토큰이면_EXPIRED_REFRESH_TOKEN() {
        when(jwtProvider.parseRefreshToken("expired-token")).thenThrow(new ExpiredJwtException(null, null, "expired"));

        assertThatThrownBy(() -> authService.reissue("expired-token"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.EXPIRED_REFRESH_TOKEN));
    }

    // 다른 키로 서명되었거나 형식이 손상된 토큰(jjwt는 SignatureException/MalformedJwtException 등
    // JwtException의 하위타입으로 던짐)도 위조로 간주해 INVALID_REFRESH_TOKEN이어야 한다
    @Test
    void 위조된_리프레시_토큰이면_INVALID_REFRESH_TOKEN() {
        when(jwtProvider.parseRefreshToken("forged-token")).thenThrow(new JwtException("서명이 유효하지 않습니다."));

        assertThatThrownBy(() -> authService.reissue("forged-token"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    // Redis에 저장된 토큰과 실제로 제시된 토큰 문자열이 다르면(재사용/탈취 의심) INVALID_REFRESH_TOKEN
    @Test
    void 저장된_토큰과_일치하지_않으면_INVALID_REFRESH_TOKEN() {
        RefreshToken savedToken = new RefreshToken(1L, "stored-token", 604800000L);

        when(claims.getSubject()).thenReturn("1");
        when(jwtProvider.parseRefreshToken("presented-token")).thenReturn(claims);
        when(refreshTokenRepository.findById(1L)).thenReturn(Optional.of(savedToken));

        assertThatThrownBy(() -> authService.reissue("presented-token"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    // ===== logout (F-02-2) =====

    // 로그아웃이 저장된 리프레시 토큰을 지운다는 전제 하에, 그 이후 같은 토큰으로 재발급을 시도하면
    // (findById가 비어있게 되어) 재발급이 실패해야 한다
    @Test
    void 로그아웃_후_같은_토큰으로_재발급을_시도하면_실패한다() {
        MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();
        when(cookieProvider.clearCookie("refreshToken")).thenReturn(ResponseCookie.from("refreshToken", "").build());

        authService.logout(1L, httpServletResponse);

        verify(refreshTokenRepository).deleteById(1L);

        when(claims.getSubject()).thenReturn("1");
        when(jwtProvider.parseRefreshToken("logged-out-token")).thenReturn(claims);
        when(refreshTokenRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.reissue("logged-out-token"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    private User createUser(Long id, String username, String encodedPassword, UserStatus status) {
        User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .nickname(username)
                .role(Role.USER)
                .status(status)
                .build();
        setId(user, id);
        return user;
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
