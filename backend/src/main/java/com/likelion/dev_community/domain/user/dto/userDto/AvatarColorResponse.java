package com.likelion.dev_community.domain.user.dto.userDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class AvatarColorResponse {

    @Schema(example = "#2563eb", description = "새로 뽑힌 아바타 색(HEX)")
    private final String avatarColorHex;
    @Schema(example = "2026-08-25T10:00:00", description = "이번에 색을 뽑은 시각")
    private final LocalDateTime avatarColorRolledAt;

    public static AvatarColorResponse of(String avatarColorHex, LocalDateTime avatarColorRolledAt) {
        return new AvatarColorResponse(avatarColorHex, avatarColorRolledAt);
    }
}
