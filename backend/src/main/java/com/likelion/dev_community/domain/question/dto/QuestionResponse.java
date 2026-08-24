package com.likelion.dev_community.domain.question.dto;

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
public class QuestionResponse {

    @Schema(example = "1", description = "질문 ID")
    private final Long id;
    @Schema(example = "3", description = "작성자 회원 ID")
    private final Long authorId;
    @Schema(example = "BE24-Team4", description = "작성자 닉네임. isAnonymous=true면 \"익명\"으로 대체됨")
    private final String authorNickname;
    @Schema(example = "Spring Security 인증 관련 질문입니다", description = "질문 제목")
    private final String title;
    @Schema(example = "JWT 토큰 검증 시 401이 발생하는데 원인을 모르겠습니다.", description = "질문 본문")
    private final String content;
    private final QuestionStatus status;

    @Schema(example = "152", description = "조회수")
    private final int viewCount;
    @Schema(example = "24", description = "추천수")
    private final int likeCount;

    private final List<String> tags;
    @Schema(example = "2026-08-23T10:00:00", description = "질문 작성일시")
    private final LocalDateTime createdAt;
    @Schema(example = "false", description = "프리미엄(구독자 전용) 게시판 글 여부")
    private final boolean isPremium;
    @Schema(example = "false", description = "익명 작성 여부")
    private final boolean isAnonymous;

    private final QuestionType type;

    public static QuestionResponse from(Question question, List<String> tagNames) {
        return new QuestionResponse(
                question.getId(),
                question.getAuthor().getId(),
                question.isAnonymous() ? "익명" : question.getAuthor().getDisplayNickname(),
                question.getTitle(),
                question.getContent(),
                question.getStatus(),

                question.getViewCount(),
                question.getLikeCount(),

                tagNames,
                question.getCreatedAt(),
                question.isPremium(),
                question.isAnonymous(),
                question.getType()
        );
    }
}