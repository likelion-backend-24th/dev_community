package com.likelion.dev_community.domain.user.dto.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OAuthCompleteRequest {
    @NotBlank(message = "signupToken은 필수입니다.")
    @Schema(example = "signup-token-3f2504e0-4f89-11d3-9a0c-0305e82c3301", description = "GitHub/Google 최초 로그인 리다이렉트 시 발급된 1회용 토큰(UUID). Redis에 20분 TTL로 저장된 임시 가입정보를 가리키는 키")
    private final String signupToken;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Schema(example = "BE24-Team4", description = "사용자가 확정한 닉네임. 다른 회원과 중복 불가")
    private final String nickname;

    // 가입 제공자(구글/깃허브)에서 이메일을 가져오지 못했을 때만 이 값을 사용한다.
    // 제공자에서 이메일을 가져온 경우 서버는 이 값을 무시하고 signupInfo에 저장된 이메일을 사용한다.
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Schema(example = "dev_user01@example.com", description = "provider가 이메일을 제공하지 못했을 때만 사용되는 선택 입력값. provider가 이미 이메일을 준 경우 이 값은 무시됨")
    private final String email;
}