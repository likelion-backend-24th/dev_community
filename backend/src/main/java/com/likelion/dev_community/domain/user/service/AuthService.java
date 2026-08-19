package com.likelion.dev_community.domain.user.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.user.dto.authDto.*;
import com.likelion.dev_community.domain.user.dto.oauthDto.GithubUserInfo;
import com.likelion.dev_community.domain.user.entity.*;
import com.likelion.dev_community.domain.user.repository.OAuthSignupInfoRepository;
import com.likelion.dev_community.domain.user.repository.RefreshTokenRepository;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import com.likelion.dev_community.security.jwt.CookieProvider;
import com.likelion.dev_community.security.jwt.JwtProvider;
import com.likelion.dev_community.security.oauth.GithubOAuthClient;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieProvider cookieProvider;
    private final GithubOAuthClient githubOAuthClient;
    private final OAuthSignupInfoRepository oauthSignupInfoRepository;

    // 회원가입
    @Transactional
    public SignUpResponse signUp(SignUpRequest request){

        if(userRepository.existsByUsername(request.getUsername()))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "사용중인 아이디입니다." + request.getUsername());

        if(userRepository.existsByNickname(request.getNickname()))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "사용중인 닉네임입니다." + request.getNickname());

        User user = User.createUser(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );

        userRepository.save(user);

        return SignUpResponse.from(user);
    }

    // 로그인
    @Transactional
    public TokenResponse signIn(SignInRequest request, HttpServletResponse httpServletResponse){
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);

        if(user.getStatus() == UserStatus.WITHDRAWN)
            throw new CustomException(ErrorCode.WITHDRAWN_ACCOUNT);

        if(user.getStatus() == UserStatus.SUSPENDED)
            throw new CustomException(ErrorCode.SUSPENDED_ACCOUNT);

        refreshTokenRepository.deleteById(user.getId());

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getUsername(), user.getNickname(), List.of(user.getRole().name()));
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(new RefreshToken(user.getId(),refreshToken,jwtProvider.getRefreshTokenExpirationMs()));

        ResponseCookie cookie = cookieProvider.createCookie("refreshToken",refreshToken, Duration.ofMillis(jwtProvider.getRefreshTokenExpirationMs()));

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return TokenResponse.of(accessToken);
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

        ResponseCookie cookie = cookieProvider.clearCookie("refreshToken");

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

    // github 로그인
    @Transactional
    public OAuthLoginResponse oauthLogin(AuthProvider provider, String code, HttpServletResponse httpServletResponse) {

        GithubUserInfo info = switch (provider) {
            case GITHUB -> githubOAuthClient.getUserInfo(code);
            default -> throw new CustomException(ErrorCode.INVALID_INPUT, "지원하지 않는 로그인 방식입니다.");
        };

        Optional<User> existing = userRepository.findByProviderAndProviderId(provider, info.getId());

        if (existing.isPresent()) {
            User user = existing.get();

            if (user.getStatus() == UserStatus.WITHDRAWN)
                throw new CustomException(ErrorCode.WITHDRAWN_ACCOUNT);
            if (user.getStatus() == UserStatus.SUSPENDED)
                throw new CustomException(ErrorCode.SUSPENDED_ACCOUNT);

            return OAuthLoginResponse.ofExistingUser(issueToken(user, httpServletResponse));
        }

        String signupToken = UUID.randomUUID().toString();
        oauthSignupInfoRepository.save(new OAuthSignupInfo(signupToken, provider.name(), info.getId()));

        return OAuthLoginResponse.ofNewUser(signupToken, info.getLogin());
    }

    // 닉네임 입력받은 뒤 실제 회원가입 + 로그인
    @Transactional
    public TokenResponse oauthComplete(String signupToken, String nickname, HttpServletResponse httpServletResponse) {

        OAuthSignupInfo signupInfo = oauthSignupInfoRepository.findById(signupToken)
                .orElseThrow(() -> new CustomException(ErrorCode.OAUTH_SIGNUP_EXPIRED));

        if (userRepository.existsByNickname(nickname))
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE, "사용중인 닉네임입니다." + nickname);

        AuthProvider provider = AuthProvider.valueOf(signupInfo.getProvider());
        String username = provider.name().toLowerCase() + "_" + signupInfo.getProviderId();
        String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());

        User user = User.createOAuthUser(username, randomPassword, nickname, provider, signupInfo.getProviderId());
        userRepository.save(user);

        oauthSignupInfoRepository.deleteById(signupToken);

        return issueToken(user, httpServletResponse);
    }

    // 로그인 공통 메서드 분리
    private TokenResponse issueToken(User user, HttpServletResponse httpServletResponse) {
        refreshTokenRepository.deleteById(user.getId());

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getUsername(), user.getNickname(), List.of(user.getRole().name()));
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken, jwtProvider.getRefreshTokenExpirationMs()));

        ResponseCookie cookie = cookieProvider.createCookie("refreshToken", refreshToken, Duration.ofMillis(jwtProvider.getRefreshTokenExpirationMs()));
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return TokenResponse.of(accessToken);
    }
}
