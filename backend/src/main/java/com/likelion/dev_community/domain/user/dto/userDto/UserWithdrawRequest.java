package com.likelion.dev_community.domain.user.dto.userDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserWithdrawRequest {
    private final String currentPassword; // OAuth 유저는 null
}
