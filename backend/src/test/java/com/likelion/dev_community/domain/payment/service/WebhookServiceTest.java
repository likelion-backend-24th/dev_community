package com.likelion.dev_community.domain.payment.service;

import com.likelion.dev_community.domain.payment.entity.webhook.WebhookEvent;
import com.likelion.dev_community.domain.payment.entity.webhook.WebhookEventStatus;
import com.likelion.dev_community.domain.payment.repository.WebhookEventRepository;
import io.portone.sdk.server.webhook.WebhookBillingKeyDataReady;
import io.portone.sdk.server.webhook.WebhookBillingKeyReady;
import io.portone.sdk.server.webhook.WebhookTransactionDataPaid;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private WebhookEventRepository webhookEventRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void 이미_처리된_웹훅이면_다시_처리하지_않는다() {
        when(webhookEventRepository.existsByWebhookId("id-1")).thenReturn(true);

        webhookService.process(paidWebhook(), "id-1", "{}");

        verify(webhookEventRepository, never()).save(any());
        verify(paymentService, never()).syncPaymentFromWebhook(anyString());
    }

    @Test
    void 결제완료_웹훅이면_결제_상태를_동기화하고_처리완료로_기록한다() {
        WebhookTransactionPaid webhook = paidWebhook();
        WebhookEvent event = WebhookEvent.receive("id-1", webhook.getClass().getSimpleName(), "DEV_COM1", "{}");
        when(webhookEventRepository.existsByWebhookId("id-1")).thenReturn(false);
        when(webhookEventRepository.save(any())).thenReturn(event);

        webhookService.process(webhook, "id-1", "{}");

        verify(paymentService).syncPaymentFromWebhook("DEV_COM1");
        assertThat(event.getStatus()).isEqualTo(WebhookEventStatus.PROCESSED);
    }

    @Test
    void 결제완료가_아닌_웹훅이면_동기화하지_않고_무시로_기록한다() {
        WebhookBillingKeyReady webhook = new WebhookBillingKeyReady(
                Instant.now(), new WebhookBillingKeyDataReady("billing-key-1", "store1"));
        WebhookEvent event = WebhookEvent.receive("id-1", webhook.getClass().getSimpleName(), null, "{}");
        when(webhookEventRepository.existsByWebhookId("id-1")).thenReturn(false);
        when(webhookEventRepository.save(any())).thenReturn(event);

        webhookService.process(webhook, "id-1", "{}");

        verify(paymentService, never()).syncPaymentFromWebhook(anyString());
        assertThat(event.getStatus()).isEqualTo(WebhookEventStatus.IGNORED);
    }

    private WebhookTransactionPaid paidWebhook() {
        return new WebhookTransactionPaid(Instant.now(), new WebhookTransactionDataPaid("DEV_COM1", "store1", "txn1"));
    }
}
