package com.likelion.dev_community.domain.user.dto.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class SignUpRequest {
    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(max = 50, message = "아이디는 50자 이하여야 합니다.")
    @Schema(example = "dev_user01", description = "로그인 아이디. 50자 이하, 다른 회원과 중복 불가")
    private final String username;

    @NotBlank
    @Size(min = 8,max = 64,message = "비밀번호는 8자 이상, 64자 이하여야 합니다.")
    @Schema(example = "Passw0rd!23", description = "로그인 비밀번호. 8자 이상 64자 이하")
    private final String password;

    @NotBlank
    @Size(max = 30, message = "닉네임은 30자 이하여야 합니다.")
    @Schema(example = "BE24-Team4", description = "닉네임. 30자 이하, 다른 회원과 중복 불가")
    private final String nickname;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 100, message = "이메일은 100자 이하여야 합니다.")
    @Schema(example = "dev_user01@example.com", description = "이메일. 100자 이하, 자체가입/소셜가입 통합해 중복 불가")
    private final String email;

    // 아이디 앞뒤 공백이 trim되지 않은 채 저장되면(예: "leocho" vs " leocho ")
    // 화면상 구분이 안 되는 별개 계정이 생겨 로그인 혼란을 유발하므로 여기서 정규화한다.
    public SignUpRequest(String username, String password, String nickname, String email) {
        this.username = username == null ? null : username.trim();
        this.password = password;
        this.nickname = nickname;
        this.email = email;
    }
}
