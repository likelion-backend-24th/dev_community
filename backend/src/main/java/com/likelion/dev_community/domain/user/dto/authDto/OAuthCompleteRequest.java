package com.likelion.dev_community.domain.user.dto.authDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OAuthCompleteRequest {
    @NotBlank(message = "signupToken은 필수입니다.")
    private final String signupToken;

    @NotBlank(message = "닉네임을 입력해주세요.")
    private final String nickname;
}