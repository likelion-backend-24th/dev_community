package com.likelion.dev_community.domain.payment.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.payment.dto.PaymentCompleteResponse;
import com.likelion.dev_community.domain.payment.dto.PaymentPrepareResponse;
import com.likelion.dev_community.domain.payment.dto.ProductInfo;
import com.likelion.dev_community.domain.payment.entity.order.Order;
import com.likelion.dev_community.domain.payment.entity.payment.Payment;
import com.likelion.dev_community.domain.payment.entity.payment.PaymentStatus;
import com.likelion.dev_community.domain.payment.entity.transaction.PaymentTransaction;
import com.likelion.dev_community.domain.payment.repository.OrderRepository;
import com.likelion.dev_community.domain.payment.repository.PaymentRepository;
import com.likelion.dev_community.domain.payment.repository.PaymentTransactionRepository;
import com.likelion.dev_community.domain.subscription.entity.PlanType;
import com.likelion.dev_community.domain.subscription.service.SubscriptionService;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import io.portone.sdk.server.payment.FailedPayment;
import io.portone.sdk.server.payment.PaidPayment;
import io.portone.sdk.server.payment.PaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final Long PREMIUM_PRICE= 4900L;

    @Value("${portone.store-id}")
    private String storeId;

    @Value("${portone.channel-key}")
    private String channelKey;

    private final PaymentClient paymentClient;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SubscriptionService subscriptionService;

    // 사전 검증
    @Transactional
    public PaymentPrepareResponse preparePayment(Long userId, PlanType planType){
        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(()-> new CustomException(ErrorCode.NOT_FOUND,"서비스 이용 가능 상태가 아닌 사용자입니다."));


        String paymentId = "DEV_" + UUID.randomUUID();
        String currency = "KRW";

        Order order = orderRepository.save(Order.create(user, planType, PREMIUM_PRICE, currency));
        paymentRepository.save(Payment.create(order, paymentId));

        List<ProductInfo> products = List.of(
                new ProductInfo(planType.name(), "프리미엄 구독", planType.name(), PREMIUM_PRICE, 1)
        );

        return PaymentPrepareResponse.of(storeId, channelKey, paymentId, "프리미엄 구독", PREMIUM_PRICE, currency, products);
    }

    // 결제 완료 검증
    @Transactional(noRollbackFor = CustomException.class)
    public PaymentCompleteResponse completePayment(String paymentId, Long userId) {
        // paymentId로 우리 Payment 조회, 동시 완료 요청이 순서대로 처리되도록 락. (없으면 404)
        Payment payment = paymentRepository.findByPaymentIdForUpdate(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        Order order = payment.getOrder();

        // 본인 결제인가? (아니면 403)
        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 결제만 확인할 수 있습니다.");
        }

        // 이미 완료된 결제면 재검증 없이 현재 상태 그대로 반환
        if (payment.getStatus() == PaymentStatus.PAID) {
            return PaymentCompleteResponse.of(payment.getPaymentId(), order.getPlanType(), payment.getStatus(), payment.getPaidAt());
        }

        LocalDateTime paidAt = verifyAndMarkPayment(payment, order);

        return PaymentCompleteResponse.of(payment.getPaymentId(), order.getPlanType(), payment.getStatus(), paidAt);
    }

    // PortOne 재조회, 검증, 기록
    private LocalDateTime verifyAndMarkPayment(Payment payment, Order order) {
        io.portone.sdk.server.payment.Payment portonePayment;
        try {
            // PortOne 재조회. (실패 시 409)
            portonePayment = paymentClient.getPayment(payment.getPaymentId()).join();
        } catch (CompletionException e) {
            // PortOne 응답 자체를 못 받아 실제 transactionId가 존재하지 않는 케이스
            log.warn("PortOne 결제 조회 실패: paymentId={}, cause={}", payment.getPaymentId(), e.getCause() != null ? e.getCause().toString() : e.toString());
            payment.markFailed();
            paymentTransactionRepository.save(
                    PaymentTransaction.fail(payment, null, order.getAmount(), order.getCurrency(), "PortOne 조회 실패")
            );
            throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED, "PortOne에서 결제 정보를 확인할 수 없습니다.");
        }

        // PortOne이 인식한 시도라면(성공/실패 불문) PortOne이 발급한 실제 transactionId를 그대로 사용
        String recognizedTransactionId = portonePayment instanceof io.portone.sdk.server.payment.Payment.Recognized recognized
                ? recognized.getTransactionId()
                : null;

        // PaidPayment인지? (결제완료 상태 건인지) + storeId / channelKey / 금액 / 통화가 우리 Order 기록과 일치하는가?
        boolean verified = portonePayment instanceof PaidPayment paid
                && paid.getStoreId().equals(storeId)
                && channelKey.equals(paid.getChannel().getKey())
                && paid.getAmount().getTotal() == order.getAmount()
                && paid.getCurrency().getValue().equals(order.getCurrency());

        // 불일치 => 트랜잭션에 fail 기록 후 409
        if (!verified) {
            payment.markFailed();
            String failReason = portonePayment instanceof FailedPayment failed && failed.getFailure() != null
                    ? failed.getFailure().getReason()
                    : "PortOne 재검증 실패";
            paymentTransactionRepository.save(
                    PaymentTransaction.fail(payment, recognizedTransactionId, order.getAmount(), order.getCurrency(), failReason)
            );
            throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }

        PaidPayment paid = (PaidPayment) portonePayment;
        LocalDateTime paidAt = LocalDateTime.ofInstant(paid.getPaidAt(), ZoneId.systemDefault());

        // 일치 => paid 시간, 주문상태 paid, 트랜잭션에 success 기록
        payment.markPaid(paidAt);
        order.complete();
        paymentTransactionRepository.save(
                PaymentTransaction.succeed(payment, paid.getTransactionId(), paid.getAmount().getTotal(), paid.getCurrency().getValue(), paidAt)
        );

        // 구독 갱신/생성
        subscriptionService.activateSubscription(order.getUser(), order.getPlanType());

        return paidAt;
    }

    // 웹훅 수신 시 결제 상태 동기화
    @Transactional(noRollbackFor = CustomException.class)
    public void syncPaymentFromWebhook(String paymentId) {
        Payment payment = paymentRepository.findByPaymentIdForUpdate(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        // 이미 완료된 결제면 재검증 x
        if (payment.getStatus() == PaymentStatus.PAID) {
            return;
        }

        verifyAndMarkPayment(payment, payment.getOrder());
    }
}
