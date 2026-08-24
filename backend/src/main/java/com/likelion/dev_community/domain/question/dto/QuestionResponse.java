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

    @Schema(example = "1")
    private final Long id;
    @Schema(example = "3")
    private final Long authorId;
    @Schema(example = "BE24-Team4")
    private final String authorNickname;
    @Schema(example = "Spring Security 인증 관련 질문입니다")
    private final String title;
    @Schema(example = "JWT 토큰 검증 시 401이 발생하는데 원인을 모르겠습니다.")
    private final String content;
    private final QuestionStatus status;

    @Schema(example = "152")
    private final int viewCount;
    @Schema(example = "24")
    private final int likeCount;

    private final List<String> tags;
    @Schema(example = "2026-08-23T10:00:00")
    private final LocalDateTime createdAt;
    @Schema(example = "false")
    private final boolean isPremium;
    @Schema(example = "false")
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