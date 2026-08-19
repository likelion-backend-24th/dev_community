package com.likelion.dev_community.domain.like.service;

import com.likelion.dev_community.domain.like.dto.LikeStatusResponse;
import com.likelion.dev_community.domain.like.entity.LikeTargetType;

import java.util.List;

public interface LikeService {

    // 토글 방식: 이미 추천했으면 취소, 안 했으면 추천.
    // 반환값은 처리 후 상태(true=추천됨, false=취소됨)로,
    // 프론트에서 버튼 상태를 바로 갱신할 수 있게 한다.
    boolean toggleLike(Long userId, LikeTargetType targetType, Long targetId);

    // 질문 상세 화면 진입 시, 질문/답변들에 대해 현재 사용자가 이미 추천했는지 일괄 조회
    LikeStatusResponse getLikeStatus(Long userId, Long questionId, List<Long> answerIds);
}
