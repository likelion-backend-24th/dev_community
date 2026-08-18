package com.likelion.dev_community.domain.payment.service;

import com.likelion.dev_community.domain.payment.entity.webhook.WebhookEvent;
import com.likelion.dev_community.domain.payment.repository.WebhookEventRepository;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookTransaction;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import io.portone.sdk.server.webhook.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private static final String PAID_EVENT_TYPE = "Transaction.Paid";

    private final WebhookVerifier webhookVerifier;
    private final WebhookEventRepository webhookEventRepository;
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    // 서명 검증
    public Webhook verify(String rawBody, String webhookId, String webhookSignature, String webhookTimestamp) throws WebhookVerificationException {
        return webhookVerifier.verify(rawBody, webhookId, webhookSignature, webhookTimestamp);
    }

    // 웹훅 기록 (서명 헤더 있을 때)
    @Transactional
    public void process(Webhook webhook, String webhookId, String rawBody) {
        if (webhookEventRepository.existsByWebhookId(webhookId)) {
            log.info("이미 처리된 웹훅입니다: {}", webhookId);
            return;
        }

        String eventType = webhook.getClass().getSimpleName();
        String paymentId = webhook instanceof WebhookTransaction transaction ? transaction.getData().getPaymentId() : null;

        WebhookEvent event = webhookEventRepository.save(WebhookEvent.receive(webhookId, eventType, paymentId, rawBody));

        if (webhook instanceof WebhookTransactionPaid && paymentId != null) {
            try {
                paymentService.syncPaymentFromWebhook(paymentId);
                event.markProcessed();
            } catch (RuntimeException e) {
                // 재검증 실패 시에도 웹훅 자체는 정상 수신
                log.warn("웹훅 결제 동기화 실패: paymentId={}, reason={}", paymentId, e.getMessage());
            }
        } else {
            event.markIgnored();
        }
    }

    // 웹훅 메시지를 신뢰하지 않고 결제 건의 상태를 포트원 API를 통해 새로 조회하여 이 응답 신뢰
    @Transactional
    public void processUnverified(String rawBody) {
        JsonNode root;
        try {
            root = objectMapper.readTree(rawBody);
        } catch (JacksonException e) {
            log.warn("웹훅 본문 파싱 실패: {}", e.getMessage());
            return;
        }

        String type = root.path("type").asString(null); // ex) Transaction.Paid
        String paymentId = root.path("data").path("paymentId").asString(null); // ex) DEV_d0bee515-...

        WebhookEvent event = webhookEventRepository.save(WebhookEvent.receive("unverified-" + UUID.randomUUID(), type != null ? type : "UNKNOWN", paymentId, rawBody));

        if (!PAID_EVENT_TYPE.equals(type) || paymentId == null) {
            event.markIgnored();
            return;
        }

        try {
            paymentService.syncPaymentFromWebhook(paymentId);
            event.markProcessed();
        } catch (RuntimeException e) {
            log.warn("no Signature 웹훅 결제 동기화 실패: paymentId={}, reason={}", paymentId, e.getMessage());
        }
    }
}
