package com.likelion.dev_community.domain.reputation.service;

import com.likelion.dev_community.domain.reputation.entity.ReputationEvent;

public interface ReputationService {

    // 활동 발생 시 점수 적립 (예: 추천받음, 답변 채택됨)
    void apply(Long userId, ReputationEvent event);

    // 활동 취소 시 적립했던 점수 회수 (예: 추천 취소, 채택 취소)
    void revert(Long userId, ReputationEvent event);
}
