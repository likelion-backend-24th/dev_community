package com.likelion.dev_community.domain.like.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LikeStatusResponse {

    @Schema(example = "true")
    private final boolean questionLiked;
    private final List<Long> likedAnswerIds;
}
