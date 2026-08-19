package com.likelion.dev_community.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PersonalDashboardSummaryResponse {

    private final long questionCount;
    private final long answerCount;
    private final long adoptedAnswerCount;
    private final long unresolvedQuestionCount;
    private final int reputation;
}
