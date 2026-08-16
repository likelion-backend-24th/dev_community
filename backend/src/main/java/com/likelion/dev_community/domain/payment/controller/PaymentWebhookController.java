package com.likelion.dev_community.domain.payment.controller;

import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final WebhookVerifier webhookVerifier;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody String rawBody,
                                              @RequestHeader(value = WebhookVerifier.HEADER_ID, required = false) String webhookId,
                                              @RequestHeader(value = WebhookVerifier.HEADER_SIGNATURE, required = false) String webhookSignature,
                                              @RequestHeader(value = WebhookVerifier.HEADER_TIMESTAMP, required = false) String webhookTimestamp) {
        Webhook webhook;
        try {
            webhook = webhookVerifier.verify(rawBody, webhookId, webhookSignature, webhookTimestamp);
        } catch (WebhookVerificationException e) {
            log.warn("웹훅 서명 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        log.info("웹훅 수신 및 검증 완료: {}", webhook.getClass().getSimpleName());

        // TODO: 웹훅 정보와 실제 결제건 비교 로직

        return ResponseEntity.ok().build();
    }
}
