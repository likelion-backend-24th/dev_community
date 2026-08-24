package com.likelion.dev_community.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PersonalDashboardSummaryResponse {

    @Schema(example = "12", description = "본인이 작성한 질문 수")
    private final long questionCount;
    @Schema(example = "34", description = "본인이 작성한 답변 수")
    private final long answerCount;
    @Schema(example = "9", description = "채택된 본인 답변 수")
    private final long adoptedAnswerCount;
    @Schema(example = "3", description = "본인이 작성한 질문 중 아직 해결되지 않은 질문 수")
    private final long unresolvedQuestionCount;
    @Schema(example = "120", description = "평판 점수")
    private final int reputation;
}
