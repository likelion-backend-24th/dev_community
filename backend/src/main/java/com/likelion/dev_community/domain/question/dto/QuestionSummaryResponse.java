package com.likelion.dev_community.domain.question.dto;

import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionSummaryResponse {

    private final Long id;
    private final String title;
    private final String authorNickname;
    private final QuestionStatus status;

    private final int viewCount;
    private final int likeCount;
    private final int answerCount;

    private final List<String> tags;
    private final LocalDateTime createdAt;
    private final boolean isPremium;
    private final boolean isAnonymous;

    public static QuestionSummaryResponse of(Question question, int answerCount, List<String> tags) {
        return new QuestionSummaryResponse(
                question.getId(),
                question.getTitle(),
                question.isAnonymous() ? "익명" : question.getAuthor().getDisplayNickname(),
                question.getStatus(),
                question.getViewCount(),
                question.getLikeCount(),
                answerCount,
                tags,
                question.getCreatedAt(),
                question.isPremium(),
                question.isAnonymous()
        );
    }
}