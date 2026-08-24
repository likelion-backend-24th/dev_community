package com.likelion.dev_community.domain.user.dto.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PasswordResetRequest {
    @NotBlank(message = "아이디를 입력해주세요.")
    @Schema(example = "dev_user01", description = "비밀번호를 재설정할 계정의 아이디")
    private final String username;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(example = "dev_user01@example.com", description = "계정에 등록된 이메일. 아이디와 함께 일치해야 재설정 메일이 발송됨")
    private final String email;
}
