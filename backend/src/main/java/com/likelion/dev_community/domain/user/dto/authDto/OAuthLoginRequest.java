package com.likelion.dev_community.domain.user.dto.authDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OAuthLoginRequest {
    @NotBlank(message = "code는 필수입니다.")
    private final String code;
}
