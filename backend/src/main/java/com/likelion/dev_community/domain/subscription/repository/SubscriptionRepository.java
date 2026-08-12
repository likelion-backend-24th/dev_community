package com.likelion.dev_community.domain.subscription.repository;

import com.likelion.dev_community.domain.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {
}
