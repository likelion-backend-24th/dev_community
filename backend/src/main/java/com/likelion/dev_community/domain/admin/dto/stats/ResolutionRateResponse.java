package com.likelion.dev_community.domain.admin.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResolutionRateResponse {

    @Schema(example = "120")
    private final long totalQuestions;
    @Schema(example = "84")
    private final long resolvedQuestions;
    @Schema(example = "70.0")
    private final double resolutionRate;

    public static ResolutionRateResponse of(long totalQuestions, long resolvedQuestions) {
        double rate = totalQuestions == 0
                ? 0.0
                : Math.round(resolvedQuestions * 1000.0 / totalQuestions) / 10.0;
        return new ResolutionRateResponse(totalQuestions, resolvedQuestions, rate);
    }
}
