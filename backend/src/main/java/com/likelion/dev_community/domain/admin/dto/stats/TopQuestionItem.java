package com.likelion.dev_community.domain.admin.dto.stats;

import com.likelion.dev_community.domain.question.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopQuestionItem {

    @Schema(example = "1", description = "질문 ID")
    private final Long id;
    @Schema(example = "Spring Security 인증 관련 질문입니다", description = "질문 제목")
    private final String title;
    @Schema(example = "152", description = "조회수")
    private final int viewCount;
    @Schema(example = "24", description = "추천수")
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
