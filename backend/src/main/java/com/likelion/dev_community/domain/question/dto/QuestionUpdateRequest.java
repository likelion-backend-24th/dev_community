package com.likelion.dev_community.domain.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionUpdateRequest {
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    @Schema(example = "Spring Security 인증 관련 질문입니다")
    private final String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Schema(example = "JWT 토큰 검증 시 401이 발생하는데 원인을 모르겠습니다.")
    private final String content;

    private final List<String> tags;

    @Schema(example = "false")
    private final Boolean isAnonymous;

    @Schema(example = "GENERAL")
    private final String type;

    public boolean isAnonymous() {
        return Boolean.TRUE.equals(isAnonymous);
    }
}