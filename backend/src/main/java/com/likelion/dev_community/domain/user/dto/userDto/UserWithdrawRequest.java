package com.likelion.dev_community.domain.user.dto.userDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserWithdrawRequest {
    @Schema(example = "Passw0rd!23", description = "현재 비밀번호. 본인 확인용(소셜 로그인 회원은 null 가능)")
    private final String currentPassword; // OAuth 유저는 null
}
