package com.likelion.dev_community.domain.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerRequest {
    @NotBlank(message = "내용을 입력해주세요.")
    @Schema(example = "이 경우엔 JwtAuthenticationFilter에서 SecurityContext를 설정하는 순서를 확인해보세요.")
    private final String content;

    @Schema(example = "false")
    private final boolean isAnonymous;
}
