package com.likelion.dev_community.domain.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LikeStatusResponse {

    private final boolean questionLiked;
    private final List<Long> likedAnswerIds;
}
