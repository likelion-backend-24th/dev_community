package com.likelion.dev_community.domain.admin.dto.stats;

import com.likelion.dev_community.domain.question.entity.Question;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StaleQuestionItem {

    @Schema(example = "1", description = "질문 ID")
    private final Long id;
    @Schema(example = "Spring Security 인증 관련 질문입니다", description = "질문 제목")
    private final String title;
    @Schema(example = "2026-08-23T10:00:00", description = "질문 등록일시")
    private final LocalDateTime createdAt;

    public static StaleQuestionItem from(Question question) {
        return new StaleQuestionItem(question.getId(), question.getTitle(), question.getCreatedAt());
    }
}
