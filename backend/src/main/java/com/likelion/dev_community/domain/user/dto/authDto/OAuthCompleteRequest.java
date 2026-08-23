package com.likelion.dev_community.domain.user.dto.authDto;

import jakarta.validation.constraints.Email;
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

    // 가입 제공자(구글/깃허브)에서 이메일을 가져오지 못했을 때만 이 값을 사용한다.
    // 제공자에서 이메일을 가져온 경우 서버는 이 값을 무시하고 signupInfo에 저장된 이메일을 사용한다.
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private final String email;
}