package com.likelion.dev_community.domain.answer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerRequest {
    @NotBlank(message = "내용을 입력해주세요.")
    @Schema(example = "이 경우엔 JwtAuthenticationFilter에서 SecurityContext를 설정하는 순서를 확인해보세요.", description = "답변 본문")
    private final String content;

    @Schema(example = "false", description = "익명 작성 여부. 멤버십(구독자 전용) 게시판 글에 한해 사용 가능")
    private final boolean isAnonymous;
}
