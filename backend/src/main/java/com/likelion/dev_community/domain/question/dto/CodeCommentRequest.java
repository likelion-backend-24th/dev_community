package com.likelion.dev_community.domain.question.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CodeCommentRequest {

    @NotNull(message = "라인 번호를 입력해주세요.")
    @Min(value = 1, message = "라인 번호는 1 이상이어야 합니다.")
    private final Integer lineNumber;

    @NotBlank(message = "내용을 입력해주세요.")
    private final String content;
}
