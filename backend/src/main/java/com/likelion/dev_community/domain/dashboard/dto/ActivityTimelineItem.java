package com.likelion.dev_community.domain.dashboard.dto;

import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.question.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ActivityTimelineItem {

    @Schema(example = "QUESTION")
    private final String type; // QUESTION | ANSWER
    @Schema(example = "10")
    private final Long questionId;
    @Schema(example = "Spring Security 인증 관련 질문입니다")
    private final String title;
    @Schema(example = "false")
    private final boolean isAdopted;
    @Schema(example = "2026-08-23T10:00:00")
    private final LocalDateTime createdAt;

    public static ActivityTimelineItem from(Question question) {
        return new ActivityTimelineItem(
                "QUESTION",
                question.getId(),
                question.getTitle(),
                false,
                question.getCreatedAt()
        );
    }

    public static ActivityTimelineItem from(Answer answer) {
        return new ActivityTimelineItem(
                "ANSWER",
                answer.getQuestion().getId(),
                answer.getContent(),
                answer.isAdopted(),
                answer.getCreatedAt()
        );
    }
}
