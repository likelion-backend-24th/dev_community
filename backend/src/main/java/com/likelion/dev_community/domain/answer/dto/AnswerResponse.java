package com.likelion.dev_community.domain.answer.dto;

import com.likelion.dev_community.domain.answer.entity.Answer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AnswerResponse {

    @Schema(example = "1")
    private final Long id;
    @Schema(example = "10")
    private final Long questionId;
    @Schema(example = "3")
    private final Long authorId;
    @Schema(example = "BE24-Team4")
    private final String authorNickname;
    @Schema(example = "false")
    private final boolean authorIsExpert;
    @Schema(example = "이 경우엔 JwtAuthenticationFilter에서 SecurityContext를 설정하는 순서를 확인해보세요.")
    private final String content;
    @Schema(example = "false")
    private final boolean isAdopted;
    @Schema(example = "5")
    private final int likeCount;
    @Schema(example = "2026-08-23T10:00:00")
    private final LocalDateTime createdAt;
    @Schema(example = "false")
    private final boolean isAnonymous;

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
                answer.isAnonymous()
        );
    }
}
