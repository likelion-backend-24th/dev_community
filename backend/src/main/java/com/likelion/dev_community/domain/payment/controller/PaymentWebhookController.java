package com.likelion.dev_community.domain.payment.controller;

import com.likelion.dev_community.domain.payment.service.WebhookService;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookVerifier;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "결제 웹훅", description = "PortOne이 결제/취소 이벤트 발생 시 호출하는 웹훅 수신 API. 인증 헤더 대신 PortOne 서명 검증(없으면 재조회 검증)으로 신뢰성을 확인")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentWebhookController {

    private final WebhookService webhookService;

    @Operation(summary = "PortOne 웹훅 수신", description = "PortOne이 결제완료(Transaction.Paid)/결제취소(Transaction.Cancelled) 등 이벤트 발생 시 호출. 서명 헤더가 있으면 검증 후 처리하고, 없으면 본문을 신뢰하지 않고 PortOne 재조회로 검증.")
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String rawBody,
                                              @RequestHeader(value = WebhookVerifier.HEADER_ID, required = false) String webhookId,
                                              @RequestHeader(value = WebhookVerifier.HEADER_SIGNATURE, required = false) String webhookSignature,
                                              @RequestHeader(value = WebhookVerifier.HEADER_TIMESTAMP, required = false) String webhookTimestamp) {
        // 서명 헤더가 없는 경로.
        // 본문을 신뢰하지 않고 PortOne 재조회로 검증.
        if (webhookId == null || webhookSignature == null || webhookTimestamp == null) {
            log.info("no Signature 웹훅 수신");
            webhookService.processUnverified(rawBody);
            return ResponseEntity.ok().build();
        }

        // 서명 헤더
        Webhook webhook;
        try {
            webhook = webhookService.verify(rawBody, webhookId, webhookSignature, webhookTimestamp);
        } catch (WebhookVerificationException e) {
            log.warn("웹훅 서명 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        webhookService.process(webhook, webhookId, rawBody);

        return ResponseEntity.ok().build();
    }
}
