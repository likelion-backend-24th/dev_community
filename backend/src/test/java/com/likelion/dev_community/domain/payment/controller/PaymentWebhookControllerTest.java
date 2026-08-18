package com.likelion.dev_community.domain.payment.controller;

import com.likelion.dev_community.domain.payment.service.WebhookService;
import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.WebhookTransactionDataPaid;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookControllerTest {

    @Mock
    private WebhookService webhookService;

    private PaymentWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentWebhookController(webhookService);
    }

    @Test
    void 서명_검증에_성공하면_200을_반환하고_처리를_위임한다() throws WebhookVerificationException {
        WebhookTransactionPaid paid = new WebhookTransactionPaid(
                Instant.now(), new WebhookTransactionDataPaid("DEV_COM1", "store1", "txn1"));
        when(webhookService.verify(anyString(), anyString(), anyString(), anyString())).thenReturn(paid);

        ResponseEntity<Void> response = controller.webhook("{}", "id-1", "v1,sig", "1700000000");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(webhookService).process(paid, "id-1", "{}");
    }

    @Test
    void 서명_검증에_실패하면_400을_반환하고_처리를_위임하지_않는다() throws WebhookVerificationException {
        when(webhookService.verify(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new WebhookVerificationException("No matching signature found", null));

        ResponseEntity<Void> response = controller.webhook("{}", "id-1", "v1,invalid", "1700000000");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(webhookService, never()).process(any(), anyString(), anyString());
    }

    @Test
    void 서명_헤더가_없으면_검증_없이_재조회_방식으로_처리하고_200을_반환한다() throws WebhookVerificationException {
        ResponseEntity<Void> response = controller.webhook("{}", null, "v1,sig", "1700000000");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(webhookService).processUnverified("{}");
        verify(webhookService, never()).verify(anyString(), any(), anyString(), anyString());
    }
}
