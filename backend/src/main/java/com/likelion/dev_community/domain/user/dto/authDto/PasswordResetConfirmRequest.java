package com.likelion.dev_community.domain.user.dto.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PasswordResetConfirmRequest {
    @NotBlank(message = "token은 필수입니다.")
    @Schema(example = "reset-token-3f2504e0-4f89-11d3-9a0c-0305e82c3301", description = "비밀번호 재설정 이메일에 담긴 1회용 토큰. Redis에 30분 TTL로 저장됨")
    private final String token;

    @NotBlank
    @Size(min = 8, max = 64, message = "비밀번호는 8자 이상, 64자 이하여야 합니다.")
    @Schema(example = "NewPassw0rd!23", description = "새로 설정할 비밀번호. 8자 이상 64자 이하")
    private final String newPassword;
}
