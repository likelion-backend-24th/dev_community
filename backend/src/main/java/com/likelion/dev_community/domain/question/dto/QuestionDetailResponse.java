package com.likelion.dev_community.domain.question.dto;

import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionDetailResponse {

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

    public static QuestionDetailResponse of(Question question, List<String> tags) {
        return new QuestionDetailResponse(
                question.getId(),
                question.getAuthor().getId(),
                question.getAuthor().getDisplayNickname(),
                question.getTitle(),
                question.getContent(),
                question.getStatus(),
                question.getViewCount(),
                question.getLikeCount(),
                tags,
                question.getCreatedAt(),
                question.isPremium()
        );
    }
}