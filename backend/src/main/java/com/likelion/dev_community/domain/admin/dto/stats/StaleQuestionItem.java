package com.likelion.dev_community.domain.admin.dto.stats;

import com.likelion.dev_community.domain.question.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StaleQuestionItem {

    private final Long id;
    private final String title;
    private final LocalDateTime createdAt;

    public static StaleQuestionItem from(Question question) {
        return new StaleQuestionItem(question.getId(), question.getTitle(), question.getCreatedAt());
    }
}
