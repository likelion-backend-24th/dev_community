package com.likelion.dev_community.domain.subscription.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.subscription.dto.SubscriptionResponse;
import com.likelion.dev_community.domain.subscription.entity.PlanType;
import com.likelion.dev_community.domain.subscription.entity.Subscription;
import com.likelion.dev_community.domain.subscription.entity.SubscriptionStatus;
import com.likelion.dev_community.domain.subscription.repository.SubscriptionRepository;
import com.likelion.dev_community.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final long SUBSCRIPTION_PERIOD_DAYS = 30; // 구독 기간 30일

    private final SubscriptionRepository subscriptionRepository;

    // 결제 성공 시 구독 갱신/생성
    @Transactional
    public void activateSubscription(User user, PlanType planType) {
        LocalDateTime now = LocalDateTime.now();

        subscriptionRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        subscription -> {
                            // 아직 만료 전이면 남은 기간에 이어서 연장, 아니면 오늘부터 새로 시작
                            boolean stillActive = subscription.getStatus() == SubscriptionStatus.ACTIVE
                                    && subscription.getExpiresAt().isAfter(now);
                            LocalDateTime base = stillActive ? subscription.getExpiresAt() : now;
                            subscription.activate(planType, now, base.plusDays(SUBSCRIPTION_PERIOD_DAYS));
                        },
                        () -> subscriptionRepository.save( // 신규 생성
                                Subscription.create(user, planType, now, now.plusDays(SUBSCRIPTION_PERIOD_DAYS))
                        )
                );
    }

    // 구독 여부 조회
    @Transactional
    public SubscriptionResponse getMySubscription(Long userId){
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .filter(this::isStillActive)
                .map(SubscriptionResponse::from)
                .orElse(null); // 구독x (만료된 경우도 포함)
    }

    // 구독 여부 판별
    @Transactional
    public boolean isActiveSubscriber(Long userId){
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .filter(this::isStillActive)
                .isPresent();
    }

    // 구독자 전용 기능 접근 체크
    @Transactional
    public void requireActiveSubscriber(Long userId, boolean isAdmin){
        if(!isAdmin && !isActiveSubscriber(userId))
            throw new CustomException(ErrorCode.FORBIDDEN, "구독자 전용 기능입니다.");
    }

    // 만료일 지연 갱신
    private boolean isStillActive(Subscription subscription) {
        if (subscription.getExpiresAt().isAfter(LocalDateTime.now())) {
            return true;
        }
        subscription.expire();
        return false;
    }
}
