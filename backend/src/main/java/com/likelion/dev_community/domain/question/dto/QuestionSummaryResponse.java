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
public class QuestionSummaryResponse {

    @Schema(example = "1")
    private final Long id;
    @Schema(example = "Spring Security 인증 관련 질문입니다")
    private final String title;
    @Schema(example = "BE24-Team4")
    private final String authorNickname;
    @Schema(example = "false")
    private final boolean authorIsExpert;
    private final QuestionStatus status;

    @Schema(example = "152")
    private final int viewCount;
    @Schema(example = "24")
    private final int likeCount;
    @Schema(example = "3")
    private final int answerCount;

    private final List<String> tags;
    @Schema(example = "2026-08-23T10:00:00")
    private final LocalDateTime createdAt;
    @Schema(example = "false")
    private final boolean isPremium;
    @Schema(example = "false")
    private final boolean isAnonymous;

    private final QuestionType type;

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
                question.getType()
        );
    }
}