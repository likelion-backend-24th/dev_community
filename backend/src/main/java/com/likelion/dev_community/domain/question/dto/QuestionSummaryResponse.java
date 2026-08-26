package com.likelion.dev_community.domain.question.dto;

import com.likelion.dev_community.common.avatar.AvatarPalette;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import com.likelion.dev_community.domain.question.entity.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionSummaryResponse {

    @Schema(example = "1", description = "질문 ID")
    private final Long id;
    @Schema(example = "Spring Security 인증 관련 질문입니다", description = "질문 제목")
    private final String title;
    @Schema(example = "BE24-Team4", description = "작성자 닉네임. isAnonymous=true면 \"익명\"으로 대체됨")
    private final String authorNickname;
    @Schema(example = "false", description = "작성자 전문가 인증 여부. 익명 작성 시 항상 false로 표시됨")
    private final boolean authorIsExpert;
    private final QuestionStatus status;

    @Schema(example = "152", description = "조회수")
    private final int viewCount;
    @Schema(example = "24", description = "추천수")
    private final int likeCount;
    @Schema(example = "3", description = "달린 답변 수")
    private final int answerCount;

    private final List<String> tags;
    @Schema(example = "2026-08-23T10:00:00", description = "질문 작성일시")
    private final LocalDateTime createdAt;
    @Schema(example = "false", description = "프리미엄(구독자 전용) 게시판 글 여부")
    private final boolean isPremium;
    @Schema(example = "false", description = "익명 작성 여부")
    private final boolean isAnonymous;

    private final QuestionType type;

    @Schema(example = "#2563eb", description = "작성자 아바타 색(HEX). 멤버십 색상을 뽑지 않았거나 비멤버면 null(프론트 기본 파란색), 익명 글이면 공용 회색으로 고정됨")
    private final String authorAvatarColor;

    public static QuestionSummaryResponse of(Question question, int answerCount, List<String> tags) {
        return new QuestionSummaryResponse(
                question.getId(),
                question.getTitle(),
                question.isAnonymous() ? "익명" : question.getAuthor().getDisplayNickname(),
                !question.isAnonymous() && question.getAuthor().isExpert(),
                question.getStatus(),
                question.getViewCount(),
                question.getLikeCount(),
                answerCount,
                tags,
                question.getCreatedAt(),
                question.isPremium(),
                question.isAnonymous(),
                question.getType(),
                AvatarPalette.resolveAuthorColor(question.isAnonymous(), question.getAuthor().getAvatarColorHex())
        );
    }
}