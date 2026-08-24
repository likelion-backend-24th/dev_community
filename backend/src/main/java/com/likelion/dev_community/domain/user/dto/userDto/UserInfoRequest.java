package com.likelion.dev_community.domain.user.dto.userDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserInfoRequest {

    @NotBlank
    @Size(max = 30, message = "닉네임은 30자 이하여야 합니다.")
    @Schema(example = "BE24-Team4", description = "변경할 닉네임. 30자 이하 필수 입력, 다른 회원과 중복 불가")
    private final String nickname;

    public UserInfoRequest(String nickname) {
        this.nickname = nickname;
    }
}
