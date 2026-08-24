package com.likelion.dev_community.domain.admin.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResolutionRateResponse {

    @Schema(example = "120", description = "전체 질문 수")
    private final long totalQuestions;
    @Schema(example = "84", description = "채택된 답변이 있는(해결된) 질문 수")
    private final long resolvedQuestions;
    @Schema(example = "70.0", description = "해결률(%). resolvedQuestions / totalQuestions * 100, 소수점 첫째 자리까지 반올림")
    private final double resolutionRate;

    public static ResolutionRateResponse of(long totalQuestions, long resolvedQuestions) {
        double rate = totalQuestions == 0
                ? 0.0
                : Math.round(resolvedQuestions * 1000.0 / totalQuestions) / 10.0;
        return new ResolutionRateResponse(totalQuestions, resolvedQuestions, rate);
    }
}
