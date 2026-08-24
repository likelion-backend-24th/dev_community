package com.likelion.dev_community.domain.user.dto.userDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserPwRequest {

    @NotBlank
    @Schema(example = "Passw0rd!23", description = "현재 비밀번호. 본인 확인용")
    private final String currentPassword;

    @NotBlank
    @Size(min = 8,max = 64,message = "비밀번호는 8자 이상, 64자 이하여야 합니다.")
    @Schema(example = "NewPassw0rd!45", description = "변경할 새 비밀번호. 8자 이상 64자 이하")
    private final String newPassword;
}
