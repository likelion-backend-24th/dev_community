package com.likelion.dev_community.domain.user.dto.userDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserInfoRequest {

    @NotBlank
    @Size(max = 30, message = "닉네임은 30자 이하여야 합니다.")
    private final String nickname;

    public UserInfoRequest(String nickname) {
        this.nickname = nickname;
    }
}
