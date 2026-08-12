package com.likelion.dev_community.domain.admin.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StaleQuestionsResponse {

    private final long count;
    private final List<StaleQuestionItem> questions;
}
