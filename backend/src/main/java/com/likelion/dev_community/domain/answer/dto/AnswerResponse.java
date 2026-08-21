package com.likelion.dev_community.domain.answer.dto;

import com.likelion.dev_community.domain.answer.entity.Answer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AnswerResponse {

    private final Long id;
    private final Long questionId;
    private final Long authorId;
    private final String authorNickname;
    private final boolean authorIsExpert;
    private final String content;
    private final boolean isAdopted;
    private final int likeCount;
    private final LocalDateTime createdAt;
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
