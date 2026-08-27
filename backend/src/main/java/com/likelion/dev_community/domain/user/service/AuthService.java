package com.likelion.dev_community.domain.user.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.common.mail.MailService;
import com.likelion.dev_community.common.viewcount.ViewerKeyResolver;
import com.likelion.dev_community.domain.user.dto.authDto.*;
import com.likelion.dev_community.domain.user.entity.*;
import com.likelion.dev_community.domain.user.repository.OAuthSignupInfoRepository;
import com.likelion.dev_community.domain.user.repository.PasswordResetTokenRepository;
import com.likelion.dev_community.domain.user.repository.RefreshTokenRepository;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import com.likelion.dev_community.security.LoginAttemptService;
import com.likelion.dev_community.security.jwt.CookieProvider;
import com.likelion.dev_community.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Duration PASSWORD_RESET_REQUEST_COOLDOWN = Duration.ofSeconds(60);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieProvider cookieProvider;
    private final OAuthSignupInfoRepository oauthSignupInfoRepository;
    private final LoginAttemptService loginAttemptService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;
    private final StringRedisTemplate redisTemplate;
    private final ViewerKeyResolver viewerKeyResolver;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // 회원가입
    @Transactional
    public SignUpResponse signUp(SignUpRequest request){

        if(userRepository.existsByUsername(request.getUsername()))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "사용중인 아이디입니다." + request.getUsername());

        if(userRepository.existsByNickname(request.getNickname()))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "사용중인 닉네임입니다." + request.getNickname());

        if(userRepository.existsByEmail(request.getEmail()))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용중인 이메일입니다." + request.getEmail());

        User user = User.createUser(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname(),
                request.getEmail()
        );

        userRepository.save(user);

        return SignUpResponse.from(user);
    }

    // 로그인
    @Transactional
    public TokenResponse signIn(SignInRequest request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        if(loginAttemptService.isLocked(request.getUsername()))
            throw new CustomException(ErrorCode.ACCOUNT_LOCKED);

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if(user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())){
            loginAttemptService.recordFailure(request.getUsername());
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        if(user.getStatus() == UserStatus.WITHDRAWN)
            throw new CustomException(ErrorCode.WITHDRAWN_ACCOUNT);

        if(user.getStatus() == UserStatus.SUSPENDED)
            throw new CustomException(ErrorCode.SUSPENDED_ACCOUNT);

        loginAttemptService.reset(request.getUsername());

        String accessToken = issueTokensAndSetCookie(user, httpServletResponse);

        // 화면에는 "이번" 로그인이 아니라 "직전" 로그인 정보를 보여줘야 하므로, 덮어쓰기 전에 먼저 읽어둔다.
        LocalDateTime previousLoginAt = user.getLastLoginAt();
        String previousLoginIp = user.getLastLoginIp();
        user.recordLogin(viewerKeyResolver.extractIp(httpServletRequest), LocalDateTime.now());

        return TokenResponse.of(accessToken, previousLoginAt, previousLoginIp);
    }

    // 토큰 재발급
    @Transactional
    public ReissueResponse reissue(String refreshToken){

        Claims claims;

        try {
            claims = jwtProvider.parseRefreshToken(refreshToken);
        }
        catch (ExpiredJwtException e){
            throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
        catch (JwtException e){
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = Long.valueOf(claims.getSubject());

        RefreshToken savedToken = refreshTokenRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        if(!savedToken.getRefreshToken().equals(refreshToken))
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

        String newAccessToken = jwtProvider.createAccessToken(
                user.getId(), user.getUsername(), user.getNickname(), List.of(user.getRole().name())
        );

        return ReissueResponse.of(newAccessToken);
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId, HttpServletResponse httpServletResponse){

        ResponseCookie cookie = cookieProvider.clearCookie(CookieProvider.REFRESH_TOKEN);

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        refreshTokenRepository.deleteById(userId);
    }

    // 아이디 중복 확인
    @Transactional(readOnly = true)
    public void checkUsername(String username){
        if(userRepository.existsByUsername(username))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "사용중인 아이디입니다." + username);
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    public void checkNickname(String nickname){
        if(userRepository.existsByNickname(nickname))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "사용중인 닉네임입니다." + nickname);
    }

    // 이메일 중복 확인
    @Transactional(readOnly = true)
    public void checkEmail(String email){
        if(userRepository.existsByEmail(email))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용중인 이메일입니다." + email);
    }

    @Transactional
    public TokenResponse oauthComplete(String signupToken, String nickname, String requestEmail, HttpServletResponse httpServletResponse) {

        OAuthSignupInfo signupInfo = oauthSignupInfoRepository.findById(signupToken)
                .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_SIGNUP_EXPIRED));

        if (userRepository.existsByNickname(nickname))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "사용중인 닉네임입니다." + nickname);

        // 구글/깃허브에서 이메일을 가져온 경우 그 값을 그대로 신뢰하고, 클라이언트가 보낸 값은 무시한다.
        // (예: 구글로 가입하면서 다른 이메일을 입력해 우회하는 것을 막기 위함)
        String email = signupInfo.getEmail() != null ? signupInfo.getEmail() : requestEmail;
        if (email == null || email.isBlank())
            throw new CustomException(ErrorCode.INVALID_INPUT, "이메일을 입력해주세요.");

        if (userRepository.existsByEmail(email))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용중인 이메일입니다." + email);

        AuthProvider provider = AuthProvider.valueOf(signupInfo.getProvider());
        String username = provider.name().toLowerCase() + "_" + signupInfo.getProviderId();
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = User.createOAuthUser(username, randomPassword, nickname, email, provider, signupInfo.getProviderId());
        userRepository.save(user);

        oauthSignupInfoRepository.deleteById(signupToken);

        return issueToken(user, httpServletResponse);
    }

    // 비밀번호 재설정 요청 - 아이디와 이메일이 같은 계정 것인지 먼저 확인한다.
    // 계정 존재 여부가 응답으로 새어나가지 않도록, 결과와 무관하게 항상 같은 응답을 준다
    @Transactional
    public void requestPasswordReset(String username, String email) {
        Boolean canSend = redisTemplate.opsForValue()
                .setIfAbsent("pwreset:cooldown:" + username, "1", PASSWORD_RESET_REQUEST_COOLDOWN);
        if (!Boolean.TRUE.equals(canSend)) return;

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();
        if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(email)) return;

        try {
            if (user.getProvider() != AuthProvider.LOCAL) {
                mailService.sendSocialAccountNotice(email, user.getProvider());
                return;
            }

            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(new PasswordResetToken(token, user.getId()));
            mailService.sendPasswordResetEmail(email, frontendUrl + "/reset-password?token=" + token);
        } catch (CustomException e) {
            // 메일 발송 실패를 그대로 응답하면 "계정이 존재한다"는 사실이 새어나가므로 로그만 남긴다
            log.warn("비밀번호 재설정 메일 발송 실패 - email={}", email, e);
        }
    }

    // 비밀번호 재설정 확정
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findById(token)
                .orElseThrow(() -> new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.PASSWORD_RESET_TOKEN_INVALID));

        user.updatePassword(passwordEncoder.encode(newPassword));
        passwordResetTokenRepository.deleteById(token);

        // 재설정 후 기존에 발급된 세션은 모두 무효화한다
        refreshTokenRepository.deleteById(user.getId());
    }

    // 로그인 공통 메서드 분리
    private TokenResponse issueToken(User user, HttpServletResponse httpServletResponse) {
        return TokenResponse.of(issueTokensAndSetCookie(user, httpServletResponse));
    }

    // 기존 세션을 정리하고 액세스/리프레시 토큰을 새로 발급한다.
    // 리프레시 토큰은 응답 쿠키로 내려보내고, 액세스 토큰만 반환한다.
    private String issueTokensAndSetCookie(User user, HttpServletResponse httpServletResponse) {
        refreshTokenRepository.deleteById(user.getId());

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getUsername(), user.getNickname(), List.of(user.getRole().name()));
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken, jwtProvider.getRefreshTokenExpirationMs()));

        ResponseCookie cookie = cookieProvider.createCookie(
                CookieProvider.REFRESH_TOKEN, refreshToken, Duration.ofMillis(jwtProvider.getRefreshTokenExpirationMs()));
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return accessToken;
    }
}