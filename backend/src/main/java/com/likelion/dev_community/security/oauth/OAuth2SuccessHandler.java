package com.likelion.dev_community.security.oauth;

import com.likelion.dev_community.domain.user.entity.*;
import com.likelion.dev_community.domain.user.repository.OAuthSignupInfoRepository;
import com.likelion.dev_community.domain.user.repository.RefreshTokenRepository;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import com.likelion.dev_community.security.jwt.CookieProvider;
import com.likelion.dev_community.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final OAuthSignupInfoRepository oauthSignupInfoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final CookieProvider cookieProvider;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        AuthProvider provider = AuthProvider.valueOf(oauthToken.getAuthorizedClientRegistrationId().toUpperCase());
        String providerId = extractProviderId(provider, oAuth2User);
        String suggestedNickname = extractNickname(provider, oAuth2User);
        String email = extractEmail(provider, oAuth2User);

        Optional<User> existing = userRepository.findByProviderAndProviderId(provider, providerId);

        if (existing.isPresent()) {
            User user = existing.get();

            // 스펙: 정지/탈퇴 계정 -> 401(SUSPENDED_ACCOUNT / WITHDRAWN_ACCOUNT)
            // 실제 401은 못 주니, 에러 코드를 쿼리스트링에 실어서 프론트가 로그인 페이지에서 표시하게 함
            if (user.getStatus() == UserStatus.SUSPENDED) {
                response.sendRedirect(frontendUrl + "/login?error=SUSPENDED_ACCOUNT");
                return;
            }
            if (user.getStatus() == UserStatus.WITHDRAWN) {
                response.sendRedirect(frontendUrl + "/login?error=WITHDRAWN_ACCOUNT");
                return;
            }

            String accessToken = issueTokens(user, response);
            response.sendRedirect(frontendUrl + "/oauth/callback?accessToken="
                    + URLEncoder.encode(accessToken, StandardCharsets.UTF_8));
            return;
        }

        String signupToken = UUID.randomUUID().toString();
        oauthSignupInfoRepository.save(new OAuthSignupInfo(signupToken, provider.name(), providerId, email));

        String redirectUrl = frontendUrl + "/oauth/nickname?signupToken=" + signupToken
                + "&nickname=" + URLEncoder.encode(suggestedNickname, StandardCharsets.UTF_8);
        if (email != null) {
            redirectUrl += "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
        }
        response.sendRedirect(redirectUrl);
    }

    private String extractProviderId(AuthProvider provider, OAuth2User user) {
        return switch (provider) {
            case GITHUB -> String.valueOf(user.getAttributes().get("id"));
            case GOOGLE -> String.valueOf(user.getAttributes().get("sub"));
            default -> throw new IllegalStateException("지원하지 않는 provider입니다.");
        };
    }

    // 구글은 항상 이메일을 내려주지만, 깃허브는 CustomOAuth2UserService가 /user/emails로 보강한
    // 경우에만 값이 있고, 그마저 실패하면 null일 수 있다(가입 완료 화면에서 직접 입력받는다).
    private String extractEmail(AuthProvider provider, OAuth2User user) {
        return switch (provider) {
            case GOOGLE -> (String) user.getAttributes().get("email");
            case GITHUB -> (String) user.getAttributes().get("email");
            default -> null;
        };
    }

    private String extractNickname(AuthProvider provider, OAuth2User user) {
        return switch (provider) {
            case GITHUB -> String.valueOf(user.getAttributes().get("login"));
            case GOOGLE -> String.valueOf(user.getAttributes().get("name"));
            default -> "user";
        };
    }

    private String issueTokens(User user, HttpServletResponse response) {
        refreshTokenRepository.deleteById(user.getId());

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getUsername(), user.getNickname(), List.of(user.getRole().name()));
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenRepository.save(new RefreshToken(user.getId(), refreshToken, jwtProvider.getRefreshTokenExpirationMs()));

        ResponseCookie cookie = cookieProvider.createCookie("refreshToken", refreshToken, Duration.ofMillis(jwtProvider.getRefreshTokenExpirationMs()));
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return accessToken;
    }
}