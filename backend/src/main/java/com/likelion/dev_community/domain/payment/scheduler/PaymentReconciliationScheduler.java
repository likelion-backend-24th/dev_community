package com.likelion.dev_community.domain.payment.scheduler;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// 정기결제 웹훅이 유실돼 상태가 갱신되지 않은 결제건을 주기적으로 PortOne에 재조회해 동기화.
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationScheduler {

    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 30 * 60 * 1000) // 30분마다
    public void reconcileStalePayments() {
        List<String> stalePaymentIds = paymentService.findStalePaymentIds();

        if (stalePaymentIds.isEmpty()) {
            return;
        }

        log.info("웹훅 유실 의심 결제건 재조회 시작: {}건", stalePaymentIds.size());

        for (String paymentId : stalePaymentIds) {
            try {
                paymentService.syncPaymentFromWebhook(paymentId);
            } catch (CustomException e) {
                log.warn("재조회 결과 실패 처리됨: paymentId={}, reason={}", paymentId, e.getMessage());
            } catch (RuntimeException e) {
                log.error("결제 재조회 중 예상치 못한 오류: paymentId={}", paymentId, e);
            }
        }
    }
}
