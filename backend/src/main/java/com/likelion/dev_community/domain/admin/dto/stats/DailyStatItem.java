package com.likelion.dev_community.domain.admin.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyStatItem {

    @Schema(example = "2026-08-23")
    private final LocalDate date;
    @Schema(example = "12")
    private final long signupCount;
    @Schema(example = "34")
    private final long questionCount;
}
