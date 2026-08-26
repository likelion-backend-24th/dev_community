package com.likelion.dev_community.domain.user.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.user.dto.authDto.ReissueResponse;
import com.likelion.dev_community.domain.user.dto.authDto.SignInRequest;
import com.likelion.dev_community.domain.user.dto.authDto.SignUpRequest;
import com.likelion.dev_community.domain.user.dto.authDto.SignUpResponse;
import com.likelion.dev_community.domain.user.dto.authDto.TokenResponse;
import com.likelion.dev_community.domain.user.entity.AuthProvider;
import com.likelion.dev_community.domain.user.entity.PasswordResetToken;
import com.likelion.dev_community.domain.user.entity.RefreshToken;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import com.likelion.dev_community.domain.user.repository.OAuthSignupInfoRepository;
import com.likelion.dev_community.domain.user.repository.RefreshTokenRepository;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import com.likelion.dev_community.common.mail.MailService;
import com.likelion.dev_community.domain.user.repository.PasswordResetTokenRepository;
import com.likelion.dev_community.security.LoginAttemptService;
import com.likelion.dev_community.security.jwt.CookieProvider;
import com.likelion.dev_community.security.jwt.JwtProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.likelion.dev_community.common.viewcount.ViewerKeyResolver;

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

    @Mock
    private OAuthSignupInfoRepository oAuthSignupInfoRepository;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private MailService mailService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ViewerKeyResolver viewerKeyResolver;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtProvider, refreshTokenRepository, cookieProvider, oAuthSignupInfoRepository, loginAttemptService, passwordResetTokenRepository, mailService, redisTemplate, viewerKeyResolver);
    }

    // ===== signUp (F-01) =====

    @Test
    void 정상적으로_회원가입한다() {
        SignUpRequest request = new SignUpRequest("newuser", "password123", "newnick", "newuser@test.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByNickname("newnick")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        SignUpResponse response = authService.signUp(request);

        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getNickname()).isEqualTo("newnick");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 아이디가_중복이면_회원가입에_실패한다() {
        SignUpRequest request = new SignUpRequest("dupuser", "password123", "newnick", "dupuser@test.com");

        when(userRepository.existsByUsername("dupuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE));
    }

    @Test
    void 닉네임이_중복이면_회원가입에_실패한다() {
        SignUpRequest request = new SignUpRequest("newuser", "password123", "dupnick", "newuser@test.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByNickname("dupnick")).thenReturn(true);

        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_RESOURCE));
    }

    @Test
    void 이메일이_중복이면_회원가입에_실패한다() {
        SignUpRequest request = new SignUpRequest("newuser", "password123", "newnick", "dup@test.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByNickname("newnick")).thenReturn(false);
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

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

        TokenResponse response = authService.signIn(request, new MockHttpServletRequest(), httpServletResponse);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(loginAttemptService).reset("tester");
    }

    @Test
    void 존재하지_않는_아이디로_로그인하면_INVALID_CREDENTIALS() {
        SignInRequest request = new SignInRequest("no-such-user", "raw-password");

        when(userRepository.findByUsername("no-such-user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signIn(request, new MockHttpServletRequest(), new MockHttpServletResponse()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));

        verify(loginAttemptService).recordFailure("no-such-user");
    }

    // 존재하지 않는 계정과 동일한 에러코드(INVALID_CREDENTIALS)를 반환해야 계정 열거 공격을 막을 수 있다
    @Test
    void 비밀번호가_틀리면_INVALID_CREDENTIALS() {
        User user = createUser(1L, "tester", "encoded-password", UserStatus.ACTIVE);
        SignInRequest request = new SignInRequest("tester", "wrong-password");

        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.signIn(request, new MockHttpServletRequest(), new MockHttpServletResponse()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS));

        verify(loginAttemptService).recordFailure("tester");
    }

    @Test
    void 로그인_시도가_잠긴_계정이면_ACCOUNT_LOCKED() {
        SignInRequest request = new SignInRequest("locked-user", "raw-password");

        when(loginAttemptService.isLocked("locked-user")).thenReturn(true);

        assertThatThrownBy(() -> authService.signIn(request, new MockHttpServletRequest(), new MockHttpServletResponse()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_LOCKED));

        verify(userRepository, org.mockito.Mockito.never()).findByUsername(any());
    }

    @Test
    void 탈퇴한_계정은_로그인이_차단된다() {
        User user = createUser(1L, "withdrawn", "encoded-password", UserStatus.WITHDRAWN);
        SignInRequest request = new SignInRequest("withdrawn", "raw-password");

        when(userRepository.findByUsername("withdrawn")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("raw-password", "encoded-password")).thenReturn(true);

        assertThatThrownBy(() -> authService.signIn(request, new MockHttpServletRequest(), new MockHttpServletResponse()))
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

    // ===== requestPasswordReset / confirmPasswordReset =====

    @Test
    void 존재하지_않는_아이디로_재설정_요청하면_메일을_보내지_않는다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        authService.requestPasswordReset("nobody", "nobody@test.com");

        verify(mailService, org.mockito.Mockito.never()).sendPasswordResetEmail(any(), any());
        verify(passwordResetTokenRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void 아이디는_존재하지만_이메일이_일치하지_않으면_메일을_보내지_않는다() {
        User user = createOAuthUser(1L, "real@test.com", AuthProvider.LOCAL);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

        authService.requestPasswordReset("tester", "wrong@test.com");

        verify(mailService, org.mockito.Mockito.never()).sendPasswordResetEmail(any(), any());
        verify(mailService, org.mockito.Mockito.never()).sendSocialAccountNotice(any(), any());
        verify(passwordResetTokenRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void 소셜_계정_아이디와_이메일이_일치하면_안내_메일만_보낸다() {
        User user = createOAuthUser(1L, "google@test.com", AuthProvider.GOOGLE);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
        when(userRepository.findByUsername("google_1")).thenReturn(Optional.of(user));

        authService.requestPasswordReset("google_1", "google@test.com");

        verify(mailService).sendSocialAccountNotice("google@test.com", AuthProvider.GOOGLE);
        verify(passwordResetTokenRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void 아이디와_이메일이_일치하는_로컬_계정이면_토큰을_저장하고_메일을_보낸다() {
        User user = createOAuthUser(1L, "local@test.com", AuthProvider.LOCAL);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
        when(userRepository.findByUsername("local_1")).thenReturn(Optional.of(user));

        authService.requestPasswordReset("local_1", "local@test.com");

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(mailService).sendPasswordResetEmail(eq("local@test.com"), any());
    }

    @Test
    void 쿨다운_중이면_재설정_요청을_무시한다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);

        authService.requestPasswordReset("local_1", "local@test.com");

        verify(userRepository, org.mockito.Mockito.never()).findByUsername(any());
    }

    @Test
    void 유효하지_않은_토큰으로_재설정하면_실패한다() {
        when(passwordResetTokenRepository.findById("bad-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.confirmPasswordReset("bad-token", "newpassword123"))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));
    }

    @Test
    void 정상적으로_비밀번호를_재설정한다() {
        User user = createUser(1L, "tester", "old-encoded-password", UserStatus.ACTIVE);
        PasswordResetToken token = new PasswordResetToken("valid-token", 1L);

        when(passwordResetTokenRepository.findById("valid-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword123")).thenReturn("new-encoded-password");

        authService.confirmPasswordReset("valid-token", "newpassword123");

        assertThat(user.getPassword()).isEqualTo("new-encoded-password");
        verify(passwordResetTokenRepository).deleteById("valid-token");
        verify(refreshTokenRepository).deleteById(1L);
    }

    private User createOAuthUser(Long id, String email, AuthProvider provider) {
        User user = User.builder()
                .username(provider.name().toLowerCase() + "_" + id)
                .password("encoded-random-password")
                .nickname("nick" + id)
                .email(email)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .provider(provider)
                .providerId(String.valueOf(id))
                .build();
        setId(user, id);
        return user;
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
