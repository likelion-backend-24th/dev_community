package com.likelion.dev_community.domain.user.dto.userDto;

import com.likelion.dev_community.domain.user.entity.AuthProvider;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class UserInfoResponse {
    private final Long userId;
    private final String username;
    private final String nickname;
    private final Role role;
    private final UserStatus status;
    private final AuthProvider provider;
    private final LocalDateTime createdAt;
    private final int reputation;
    private final boolean isExpert;

    public static UserInfoResponse from(User user){
        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRole(),
                user.getStatus(),
                user.getProvider(),
                user.getCreatedAt(),
                user.getReputation(),
                user.isExpert()
        );
    }
}
