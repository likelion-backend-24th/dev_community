package com.likelion.dev_community.domain.admin.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyStatItem {

    private final LocalDate date;
    private final long signupCount;
    private final long questionCount;
}
