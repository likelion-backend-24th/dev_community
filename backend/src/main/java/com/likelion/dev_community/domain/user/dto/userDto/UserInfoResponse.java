package com.likelion.dev_community.domain.user.dto.userDto;

import com.likelion.dev_community.domain.user.entity.AuthProvider;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class UserInfoResponse {
    @Schema(example = "1")
    private final Long userId;
    @Schema(example = "dev_user01")
    private final String username;
    @Schema(example = "BE24-Team4")
    private final String nickname;
    private final Role role;
    private final UserStatus status;
    private final AuthProvider provider;
    @Schema(example = "2026-08-23T10:00:00")
    private final LocalDateTime createdAt;
    @Schema(example = "120")
    private final int reputation;
    @Schema(example = "false")
    private final boolean isExpert;
    @Schema(example = "false")
    private final boolean expertRequested;

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
                user.isExpert(),
                user.isExpertRequested()
        );
    }
}
