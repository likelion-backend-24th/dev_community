package com.likelion.dev_community.domain.answer.dto;

import com.likelion.dev_community.common.avatar.AvatarPalette;
import com.likelion.dev_community.domain.answer.entity.Answer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AnswerResponse {

    @Schema(example = "1", description = "답변 ID")
    private final Long id;
    @Schema(example = "10", description = "이 답변이 달린 질문 ID")
    private final Long questionId;
    @Schema(example = "3", description = "작성자 회원 ID")
    private final Long authorId;
    @Schema(example = "BE24-Team4", description = "작성자 닉네임. isAnonymous=true면 \"익명\"으로 대체됨")
    private final String authorNickname;
    @Schema(example = "false", description = "작성자 전문가 인증 여부. 익명 작성 시 항상 false로 표시됨")
    private final boolean authorIsExpert;
    @Schema(example = "이 경우엔 JwtAuthenticationFilter에서 SecurityContext를 설정하는 순서를 확인해보세요.", description = "답변 본문")
    private final String content;
    @Schema(example = "false", description = "질문 작성자에게 채택되었는지 여부")
    private final boolean isAdopted;
    @Schema(example = "5", description = "추천수")
    private final int likeCount;
    @Schema(example = "2026-08-23T10:00:00", description = "답변 작성일시")
    private final LocalDateTime createdAt;
    @Schema(example = "false", description = "익명 작성 여부")
    private final boolean isAnonymous;

    @Schema(example = "#2563eb", description = "작성자 아바타 색(HEX). 멤버십 색상을 뽑지 않았거나 비멤버면 null(프론트 기본 파란색), 익명 글이면 공용 회색으로 고정됨")
    private final String authorAvatarColor;

    public static AnswerResponse from(Answer answer) {
        return new AnswerResponse(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getAuthor().getId(),
                answer.isAnonymous() ? "익명" : answer.getAuthor().getDisplayNickname(),
                !answer.isAnonymous() && answer.getAuthor().isExpert(),
                answer.getContent(),
                answer.isAdopted(),
                answer.getLikeCount(),
                answer.getCreatedAt(),
                answer.isAnonymous(),
                AvatarPalette.resolveAuthorColor(answer.isAnonymous(), answer.getAuthor().getAvatarColorHex())
        );
    }
}
