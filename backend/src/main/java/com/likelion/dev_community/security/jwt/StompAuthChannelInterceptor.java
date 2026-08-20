package com.likelion.dev_community.security.jwt;

import com.likelion.dev_community.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

// STOMP는 CONNECT 프레임에 커스텀 헤더(Authorization)를 실을 수 있어서(네이티브 WebSocket/SSE와
// 달리 핸드셰이크 단계에서 헤더 전달이 안 되는 문제를 피할 수 있음), 여기서 JWT를 꺼내 인증한다.
// 이후 이 메시지의 Principal이 STOMP 세션에 그대로 연결돼서 convertAndSendToUser가 동작한다.
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor.getFirstNativeHeader("Authorization"));

            if (token == null) {
                throw new IllegalArgumentException("인증 토큰이 없습니다.");
            }

            try {
                Claims claims = jwtProvider.parseAccessToken(token);

                Long userId = Long.valueOf(claims.getSubject());
                String username = claims.get("username", String.class);
                String nickname = claims.get("nickname", String.class);
                List<String> roles = claims.get("roles", List.class);

                List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                        .toList();

                CustomUserDetails userDetails = new CustomUserDetails(userId, username, null, nickname, authorities);
                accessor.setUser(new JwtAuthenticationToken(userDetails, authorities));
            } catch (JwtException e) {
                throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
            }
        }

        return message;
    }

    private String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
