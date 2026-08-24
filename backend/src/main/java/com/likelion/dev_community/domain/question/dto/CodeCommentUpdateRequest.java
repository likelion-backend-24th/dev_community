package com.likelion.dev_community.domain.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CodeCommentUpdateRequest {

    @NotBlank(message = "내용을 입력해주세요.")
    @Schema(example = "이 부분은 null 체크가 필요해 보입니다.")
    private final String content;

}
