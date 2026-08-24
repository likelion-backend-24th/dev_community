package com.likelion.dev_community.domain.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TagSuggestRequest {
    @Schema(example = "Spring Security 인증 관련 질문입니다")
    private final String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Schema(example = "JWT 토큰 검증 시 401이 발생하는데 원인을 모르겠습니다.")
    private final String content;
}
