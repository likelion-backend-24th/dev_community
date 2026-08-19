package com.likelion.dev_community.domain.admin.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResolutionRateResponse {

    private final long totalQuestions;
    private final long resolvedQuestions;
    private final double resolutionRate;

    public static ResolutionRateResponse of(long totalQuestions, long resolvedQuestions) {
        double rate = totalQuestions == 0
                ? 0.0
                : Math.round(resolvedQuestions * 1000.0 / totalQuestions) / 10.0;
        return new ResolutionRateResponse(totalQuestions, resolvedQuestions, rate);
    }
}
