package com.likelion.dev_community.domain.admin.dto.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class StaleQuestionsResponse {

    @Schema(example = "5")
    private final long count;
    private final List<StaleQuestionItem> questions;
}
