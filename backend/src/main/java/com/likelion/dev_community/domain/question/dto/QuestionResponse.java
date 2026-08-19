package com.likelion.dev_community.domain.question.dto;

import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionResponse {

    private final Long id;
    private final Long authorId;
    private final String authorNickname;
    private final String title;
    private final String content;
    private final QuestionStatus status;

    private final int viewCount;
    private final int likeCount;

    private final List<String> tags;
    private final LocalDateTime createdAt;
    private final boolean isPremium;
    private final boolean isAnonymous;

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
                question.isAnonymous()
        );
    }
}