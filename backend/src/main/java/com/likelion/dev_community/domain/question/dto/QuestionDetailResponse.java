package com.likelion.dev_community.domain.question.dto;

import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import com.likelion.dev_community.domain.question.entity.QuestionType;
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
    private final boolean authorIsExpert;

    private final String title;
    private final String content;
    private final QuestionStatus status;

    private final int viewCount;
    private final int likeCount;

    private final List<String> tags;
    private final LocalDateTime createdAt;
    private final boolean isPremium;
    private final boolean isAnonymous;

    private final QuestionType type;
    private final boolean typeLocked;
    private final Long myChatRoomId;

    public static QuestionDetailResponse of(Question question, List<String> tags) {
        return of(question, tags, null);
    }

    public static QuestionDetailResponse of(Question question, List<String> tags, Long myChatRoomId) {
        return new QuestionDetailResponse(
                question.getId(),
                question.getAuthor().getId(),
                question.isAnonymous() ? "익명" : question.getAuthor().getDisplayNickname(),
                !question.isAnonymous() && question.getAuthor().isExpert(),
                question.getTitle(),
                question.getContent(),
                question.getStatus(),
                question.getViewCount(),
                question.getLikeCount(),
                tags,
                question.getCreatedAt(),
                question.isPremium(),
                question.isAnonymous(),
                question.getType(),
                question.isTypeLocked(),
                myChatRoomId
        );
    }
}