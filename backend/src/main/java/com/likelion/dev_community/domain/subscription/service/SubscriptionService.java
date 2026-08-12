package com.likelion.dev_community.domain.subscription.service;

import com.likelion.dev_community.domain.subscription.dto.SubscriptionResponse;
import com.likelion.dev_community.domain.subscription.entity.SubscriptionStatus;
import com.likelion.dev_community.domain.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    // 구독 여부 조회
    @Transactional(readOnly = true)
    public SubscriptionResponse getMySubscription(Long userId){
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .map(SubscriptionResponse::from)
                .orElse(null); // 구독x
    }

    // 구독 여부 판별
    @Transactional(readOnly = true)
    public boolean isActiveSubscriber(Long userId){
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .filter(subs -> subs.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }
}
