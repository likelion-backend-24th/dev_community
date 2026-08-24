package com.likelion.dev_community.domain.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PersonalDashboardSummaryResponse {

    @Schema(example = "12")
    private final long questionCount;
    @Schema(example = "34")
    private final long answerCount;
    @Schema(example = "9")
    private final long adoptedAnswerCount;
    @Schema(example = "3")
    private final long unresolvedQuestionCount;
    @Schema(example = "120")
    private final int reputation;
}
