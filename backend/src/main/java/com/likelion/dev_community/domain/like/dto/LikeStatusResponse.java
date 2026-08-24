package com.likelion.dev_community.domain.like.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LikeStatusResponse {

    @Schema(example = "true", description = "본인이 이 질문을 추천했는지 여부")
    private final boolean questionLiked;
    private final List<Long> likedAnswerIds;
}
