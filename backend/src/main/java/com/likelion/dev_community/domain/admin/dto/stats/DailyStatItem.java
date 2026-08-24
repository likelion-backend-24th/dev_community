package com.likelion.dev_community.domain.admin.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyStatItem {

    @Schema(example = "2026-08-23", description = "집계 대상 일자")
    private final LocalDate date;
    @Schema(example = "12", description = "해당 일자의 신규 가입자 수")
    private final long signupCount;
    @Schema(example = "34", description = "해당 일자의 질문 등록 수")
    private final long questionCount;
}
