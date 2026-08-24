package com.likelion.dev_community.domain.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(example = "12", description = "코멘트를 남길 코드 라인 번호. 1 이상")
    private final Integer lineNumber;

    @NotBlank(message = "내용을 입력해주세요.")
    @Schema(example = "이 부분은 null 체크가 필요해 보입니다.", description = "코멘트 내용")
    private final String content;
}
