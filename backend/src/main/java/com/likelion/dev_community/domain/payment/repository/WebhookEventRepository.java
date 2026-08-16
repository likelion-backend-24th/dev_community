package com.likelion.dev_community.domain.payment.repository;

import com.likelion.dev_community.domain.payment.entity.webhook.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
}
