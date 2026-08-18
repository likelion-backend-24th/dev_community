package com.likelion.dev_community.domain.dashboard.dto;

import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.question.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ActivityTimelineItem {

    private final String type; // QUESTION | ANSWER
    private final Long questionId;
    private final String title;
    private final boolean isAdopted;
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
