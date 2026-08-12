package com.likelion.dev_community.domain.subscription.repository;

import com.likelion.dev_community.domain.subscription.entity.Subscription;
import com.likelion.dev_community.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
    Optional<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status);
}
