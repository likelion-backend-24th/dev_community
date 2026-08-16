package com.likelion.dev_community.domain.payment.controller;

import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.WebhookTransactionDataPaid;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import io.portone.sdk.server.webhook.WebhookVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookControllerTest {

    @Mock
    private WebhookVerifier webhookVerifier;

    private PaymentWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new PaymentWebhookController(webhookVerifier);
    }

    @Test
    void 서명_검증에_성공하면_200을_반환한다() throws WebhookVerificationException {
        WebhookTransactionPaid paid = new WebhookTransactionPaid(
                Instant.now(), new WebhookTransactionDataPaid("DEV_COM1", "store1", "txn1"));
        when(webhookVerifier.verify(anyString(), anyString(), anyString(), anyString())).thenReturn(paid);

        ResponseEntity<Void> response = controller.handleWebhook("{}", "id-1", "v1,sig", "1700000000");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void 서명_검증에_실패하면_400을_반환한다() throws WebhookVerificationException {
        when(webhookVerifier.verify(anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new WebhookVerificationException("No matching signature found", null));

        ResponseEntity<Void> response = controller.handleWebhook("{}", "id-1", "v1,invalid", "1700000000");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void 필수_헤더가_없으면_400을_반환한다() throws WebhookVerificationException {
        when(webhookVerifier.verify(anyString(), isNull(), anyString(), anyString()))
                .thenThrow(new WebhookVerificationException("Missing required headers", null));

        ResponseEntity<Void> response = controller.handleWebhook("{}", null, "v1,sig", "1700000000");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
