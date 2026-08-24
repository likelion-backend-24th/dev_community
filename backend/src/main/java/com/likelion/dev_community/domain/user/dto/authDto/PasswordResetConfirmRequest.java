package com.likelion.dev_community.domain.user.dto.authDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PasswordResetConfirmRequest {
    @NotBlank(message = "token은 필수입니다.")
    private final String token;

    @NotBlank
    @Size(min = 8, max = 64, message = "비밀번호는 8자 이상, 64자 이하여야 합니다.")
    private final String newPassword;
}
