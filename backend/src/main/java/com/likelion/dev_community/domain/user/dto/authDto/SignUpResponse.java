package com.likelion.dev_community.domain.user.dto.authDto;

import com.likelion.dev_community.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SignUpResponse {
    @Schema(example = "dev_user01")
    private final String username;
    @Schema(example = "BE24-Team4")
    private final String nickname;

    public static SignUpResponse from(User user){
        return new SignUpResponse(
                user.getUsername(),
                user.getNickname()
        );
    }
}
