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
    @Schema(example = "1", description = "회원 고유 ID")
    private final Long userId;
    @Schema(example = "dev_user01", description = "로그인 아이디")
    private final String username;
    @Schema(example = "BE24-Team4", description = "닉네임")
    private final String nickname;
    private final Role role;
    private final UserStatus status;
    private final AuthProvider provider;
    @Schema(example = "2026-08-23T10:00:00", description = "가입일시")
    private final LocalDateTime createdAt;
    @Schema(example = "120", description = "평판 점수. 추천/채택 등 활동에 따라 자동 반영됨")
    private final int reputation;
    @Schema(example = "false", description = "전문가 인증 여부")
    private final boolean isExpert;
    @Schema(example = "false", description = "전문가 등급 신청 후 관리자 승인 대기 중인지 여부")
    private final boolean expertRequested;
    @Schema(example = "#2563eb", description = "멤버십 아바타 색(HEX). 색을 뽑은 적 없으면 null(기본 파란색)")
    private final String avatarColorHex;
    @Schema(example = "2026-08-25T10:00:00", description = "마지막으로 아바타 색을 뽑은 시각. 없으면 null")
    private final LocalDateTime avatarColorRolledAt;

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
                user.isExpertRequested(),
                user.getAvatarColorHex(),
                user.getAvatarColorRolledAt()
        );
    }
}
