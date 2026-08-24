package com.likelion.dev_community.security.jwt;

import com.likelion.dev_community.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StompAuthChannelInterceptorTest {

    private static final String ACCESS_SECRET =
            Base64.getEncoder().encodeToString("access-secret-key-for-stomp-interceptor-test".getBytes());
    private static final String REFRESH_SECRET =
            Base64.getEncoder().encodeToString("refresh-secret-key-for-stomp-interceptor-test".getBytes());

    private JwtProvider jwtProvider;
    private StompAuthChannelInterceptor interceptor;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(ACCESS_SECRET, 60_000L, REFRESH_SECRET, 3_600_000L);
        interceptor = new StompAuthChannelInterceptor(jwtProvider);
    }

    @Test
    void 유효한_토큰으로_CONNECT하면_Principal이_설정된다() {
        String token = jwtProvider.createAccessToken(1L, "leocho", "레오", List.of("USER"));
        Message<?> message = connectMessageWithAuthHeader("Bearer " + token);

        interceptor.preSend(message, null);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        JwtAuthenticationToken principal = (JwtAuthenticationToken) accessor.getUser();
        CustomUserDetails userDetails = (CustomUserDetails) principal.getPrincipal();

        assertThat(userDetails.getId()).isEqualTo(1L);
        assertThat(userDetails.getUsername()).isEqualTo("leocho");
        assertThat(userDetails.getNickname()).isEqualTo("레오");
        assertThat(userDetails.getAuthorities()).extracting(a -> a.getAuthority()).containsExactly("ROLE_USER");
    }

    @Test
    void ADMIN_권한이_그대로_ROLE_ADMIN으로_변환된다() {
        String token = jwtProvider.createAccessToken(1L, "admin", "관리자", List.of("USER", "ADMIN"));
        Message<?> message = connectMessageWithAuthHeader("Bearer " + token);

        interceptor.preSend(message, null);

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        CustomUserDetails userDetails = (CustomUserDetails) ((JwtAuthenticationToken) accessor.getUser()).getPrincipal();

        assertThat(userDetails.isAdmin()).isTrue();
    }

    @Test
    void Authorization_헤더가_없으면_예외가_발생한다() {
        Message<?> message = connectMessageWithAuthHeader(null);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void Bearer_접두사가_없으면_예외가_발생한다() {
        String token = jwtProvider.createAccessToken(1L, "leocho", "레오", List.of("USER"));
        Message<?> message = connectMessageWithAuthHeader(token);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 위조되거나_다른_시크릿으로_서명된_토큰이면_예외가_발생한다() {
        String otherSecret = Base64.getEncoder().encodeToString("completely-different-secret-key-value".getBytes());
        JwtProvider otherProvider = new JwtProvider(otherSecret, 60_000L, REFRESH_SECRET, 3_600_000L);
        String token = otherProvider.createAccessToken(1L, "leocho", "레오", List.of("USER"));
        Message<?> message = connectMessageWithAuthHeader("Bearer " + token);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 만료된_토큰이면_예외가_발생한다() {
        JwtProvider shortLivedProvider = new JwtProvider(ACCESS_SECRET, -1_000L, REFRESH_SECRET, 3_600_000L);
        String expiredToken = shortLivedProvider.createAccessToken(1L, "leocho", "레오", List.of("USER"));
        Message<?> message = connectMessageWithAuthHeader("Bearer " + expiredToken);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void CONNECT가_아닌_프레임은_인증_없이_그대로_통과한다() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        Message<?> message = org.springframework.messaging.support.MessageBuilder
                .createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, null);

        assertThat(result).isSameAs(message);
    }

    private Message<?> connectMessageWithAuthHeader(String authorizationHeaderValue) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setLeaveMutable(true);
        if (authorizationHeaderValue != null) {
            accessor.setNativeHeader("Authorization", authorizationHeaderValue);
        }
        return org.springframework.messaging.support.MessageBuilder
                .createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
