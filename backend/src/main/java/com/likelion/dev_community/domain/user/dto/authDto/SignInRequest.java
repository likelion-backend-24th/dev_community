package com.likelion.dev_community.domain.user.dto.authDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SignInRequest {
    @NotBlank
    @Schema(example = "dev_user01", description = "로그인 아이디")
    private final String username;

    @NotBlank
    @Schema(example = "Passw0rd!23", description = "로그인 비밀번호")
    private final String password;
}
