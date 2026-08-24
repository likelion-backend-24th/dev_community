package com.likelion.dev_community.domain.question.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionCreateRequest {
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    @Schema(example = "Spring Security 인증 관련 질문입니다", description = "질문 제목. 100자 이하")
    private final String title;

    @NotBlank(message = "내용을 입력해주세요.")
    @Schema(example = "JWT 토큰 검증 시 401이 발생하는데 원인을 모르겠습니다.", description = "질문 본문. 마크다운 문법 지원")
    private final String content;

    private final List<String> tags;

    @Schema(example = "false", description = "프리미엄(구독자 전용) 게시판에 올릴지 여부. 구독자(또는 ADMIN)만 true 가능")
    private final Boolean isPremium;

    @Schema(example = "false", description = "익명 작성 여부. 프리미엄 게시판 글에 한해 구독자(또는 ADMIN)만 true 가능")
    private final Boolean isAnonymous;

    @Schema(example = "GENERAL", description = "질문 유형. GENERAL, CODE_REVIEW, CAREER_CONSULT 중 하나. 일반 게시판은 항상 GENERAL 고정, 프리미엄 게시판만 CODE_REVIEW/CAREER_CONSULT 선택 가능")
    private final String type;

    public boolean isPremium() {
        return Boolean.TRUE.equals(isPremium);
    }

    public boolean isAnonymous() {
        return Boolean.TRUE.equals(isAnonymous);
    }
}