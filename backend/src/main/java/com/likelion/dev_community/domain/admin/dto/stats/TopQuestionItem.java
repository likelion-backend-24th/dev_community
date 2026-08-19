package com.likelion.dev_community.domain.admin.dto.stats;

import com.likelion.dev_community.domain.question.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopQuestionItem {

    private final Long id;
    private final String title;
    private final int viewCount;
    private final int likeCount;

    public static TopQuestionItem from(Question question) {
        return new TopQuestionItem(
                question.getId(),
                question.getTitle(),
                question.getViewCount(),
                question.getLikeCount()
        );
    }
}
