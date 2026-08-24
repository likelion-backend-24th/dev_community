package com.likelion.dev_community.domain.user.dto.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OAuthCompleteRequest {
    @NotBlank(message = "signupToken은 필수입니다.")
    @Schema(example = "signup-token-3f2504e0-4f89-11d3-9a0c-0305e82c3301")
    private final String signupToken;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Schema(example = "BE24-Team4")
    private final String nickname;
}