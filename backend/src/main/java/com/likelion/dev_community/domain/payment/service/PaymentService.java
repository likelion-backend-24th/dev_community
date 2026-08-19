package com.likelion.dev_community.domain.payment.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.payment.dto.BillingKeyPrepareResponse;
import com.likelion.dev_community.domain.payment.dto.PaymentCancelResponse;
import com.likelion.dev_community.domain.payment.dto.PaymentCompleteResponse;
import com.likelion.dev_community.domain.payment.dto.PaymentPrepareResponse;
import com.likelion.dev_community.domain.payment.dto.ProductInfo;
import com.likelion.dev_community.domain.payment.entity.order.Order;
import com.likelion.dev_community.domain.payment.entity.payment.Payment;
import com.likelion.dev_community.domain.payment.entity.payment.PaymentStatus;
import com.likelion.dev_community.domain.payment.repository.OrderRepository;
import com.likelion.dev_community.domain.payment.repository.PaymentRepository;
import com.likelion.dev_community.domain.subscription.entity.PlanType;
import com.likelion.dev_community.domain.subscription.entity.Subscription;
import com.likelion.dev_community.domain.subscription.service.SubscriptionService;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import io.portone.sdk.server.common.CustomerInput;
import io.portone.sdk.server.common.Currency;
import io.portone.sdk.server.common.PaymentAmountInput;
import io.portone.sdk.server.payment.CancelPaymentResponse;
import io.portone.sdk.server.payment.CancelRequester;
import io.portone.sdk.server.payment.PaidPayment;
import io.portone.sdk.server.payment.PayWithBillingKeyResponse;
import io.portone.sdk.server.payment.PaymentCancellation;
import io.portone.sdk.server.payment.PaymentClient;
import io.portone.sdk.server.payment.billingkey.BillingKeyClient;
import io.portone.sdk.server.payment.billingkey.BillingKeyDeleteRequester;
import io.portone.sdk.server.payment.billingkey.BillingKeyInfo;
import io.portone.sdk.server.payment.billingkey.IssuedBillingKeyInfo;
import io.portone.sdk.server.payment.paymentschedule.BillingKeyPaymentScheduleInput;
import io.portone.sdk.server.payment.paymentschedule.PaymentScheduleClient;
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
    private static final String CURRENCY = "KRW";
    private static final String SUBSCRIPTION_ORDER_NAME = "프리미엄 구독 정기결제";
    private static final long CANCELLABLE_DAYS = 7; // 결제 취소(환불) 가능 기간

    @Value("${portone.store-id}")
    private String storeId;

    @Value("${portone.channel-key}")
    private String channelKey;

    @Value("${portone.webhook-notice-url}")
    private String webhookNoticeUrl;

    private final PaymentClient paymentClient;
    private final BillingKeyClient billingKeyClient;
    private final PaymentScheduleClient paymentScheduleClient;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;

    // 사전 검증
    @Deprecated
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

    // 빌링키 발급 사전 준비
    @Transactional
    public BillingKeyPrepareResponse prepareBillingKeyIssue(Long userId) {
        userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "서비스 이용 가능 상태가 아닌 사용자입니다."));

        String issueId = "ISSUE_" + UUID.randomUUID();

        return BillingKeyPrepareResponse.of(storeId, channelKey, issueId);
    }

    // 빌링키 발급 검증 + 첫 결제 + 다음 회차 예약
    @Transactional(noRollbackFor = CustomException.class)
    public PaymentCompleteResponse issueBillingKeyAndCharge(String billingKey, Long userId, PlanType planType) {
        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "서비스 이용 가능 상태가 아닌 사용자입니다."));

        // 빌링키 재조회
        BillingKeyInfo billingKeyInfo;
        try {
            billingKeyInfo = billingKeyClient.getBillingKeyInfo(billingKey).join();
        } catch (CompletionException e) {
            log.warn("PortOne 빌링키 조회 실패: billingKey={}, cause={}", billingKey, e.getCause() != null ? e.getCause().toString() : e.toString());
            throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED, "PortOne에서 빌링키 정보를 확인할 수 없습니다.");
        }

        if (!(billingKeyInfo instanceof IssuedBillingKeyInfo issued) || !issued.getStoreId().equals(storeId)) {
            throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED, "빌링키 발급 검증에 실패했습니다.");
        }

        // 1회차 결제
        Order order = orderRepository.save(Order.create(user, planType, PREMIUM_PRICE, CURRENCY));
        String paymentId = "DEV_" + UUID.randomUUID();
        Payment payment = paymentRepository.save(Payment.create(order, paymentId));

        PayWithBillingKeyResponse response;
        try {
            response = paymentClient.payWithBillingKey(
                    paymentId, billingKey, channelKey, SUBSCRIPTION_ORDER_NAME,
                    billingCustomer(user), null,
                    new PaymentAmountInput(PREMIUM_PRICE, null, null), Currency.Krw.INSTANCE,
                    null, null, null, null, null, noticeUrls(), null, null, null, null, null, null, null, null
            ).join();
        } catch (CompletionException e) {
            log.warn("정기결제 첫 결제 실패: userId={}, cause={}", userId, e.getCause() != null ? e.getCause().toString() : e.toString());
            payment.markFailed();
            throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED, "정기결제 첫 결제에 실패했습니다.");
        }

        LocalDateTime paidAt = LocalDateTime.ofInstant(response.getPayment().getPaidAt(), ZoneId.systemDefault());
        payment.markPaid(paidAt);
        order.complete();

        Subscription subscription = subscriptionService.activateSubscription(user, planType);
        subscription.registerBillingKey(billingKey);

        scheduleNextCharge(user, planType, billingKey, subscription.getExpiresAt());

        return PaymentCompleteResponse.of(payment.getPaymentId(), planType, payment.getStatus(), paidAt);
    }

    // 다음 회차 결제 건 생성 + PortOne 결제 예약 등록
    private void scheduleNextCharge(User user, PlanType planType, String billingKey, LocalDateTime timeToPay) {
        Order nextOrder = orderRepository.save(Order.create(user, planType, PREMIUM_PRICE, CURRENCY));
        String nextPaymentId = "DEV_" + UUID.randomUUID();
        paymentRepository.save(Payment.create(nextOrder, nextPaymentId));

        BillingKeyPaymentScheduleInput scheduleInput = new BillingKeyPaymentScheduleInput(
                storeId, billingKey, channelKey, SUBSCRIPTION_ORDER_NAME,
                billingCustomer(user), null,
                new PaymentAmountInput(PREMIUM_PRICE, null, null), Currency.Krw.INSTANCE,
                null, null, null, null, null, noticeUrls(), null, null, null, null, null, null, null
        );

        try {
            paymentScheduleClient.createPaymentSchedule(
                    nextPaymentId, scheduleInput, timeToPay.atZone(ZoneId.systemDefault()).toInstant()
            ).join();
        } catch (CompletionException e) {
            log.warn("다음 정기결제 예약 실패: userId={}, cause={}", user.getId(), e.getCause() != null ? e.getCause().toString() : e.toString());
        }
    }

    private CustomerInput billingCustomer(User user) {
        // customerKey는 2자 이상, 255자 이하
        return new CustomerInput("user-" + user.getId(), null, null, null, null, null, null, null, null, null, null, null);
    }

    private List<String> noticeUrls() {
        return webhookNoticeUrl == null || webhookNoticeUrl.isBlank() ? null : List.of(webhookNoticeUrl);
    }


    // 결제 완료 검증
    @Deprecated
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
            throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED, "PortOne에서 결제 정보를 확인할 수 없습니다.");
        }

        // PaidPayment인지? (결제완료 상태 건인지) + storeId / channelKey / 금액 / 통화가 우리 Order 기록과 일치하는가?
        boolean verified = portonePayment instanceof PaidPayment paid
                && paid.getStoreId().equals(storeId)
                && channelKey.equals(paid.getChannel().getKey())
                && paid.getAmount().getTotal() == order.getAmount()
                && paid.getCurrency().getValue().equals(order.getCurrency());

        // 불일치 => 409
        if (!verified) {
            payment.markFailed();
            throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }

        PaidPayment paid = (PaidPayment) portonePayment;
        LocalDateTime paidAt = LocalDateTime.ofInstant(paid.getPaidAt(), ZoneId.systemDefault());

        // 일치 => paid 시간, 주문상태 paid
        payment.markPaid(paidAt);
        order.complete();

        // 구독 갱신/생성
        Subscription subscription = subscriptionService.activateSubscription(order.getUser(), order.getPlanType());

        // 정기결제 구독일때 다음 회차를 이어서 예약 (예약된 결제가 완료된 뒤 다음 결제를 예약)
        if (subscription.getBillingKey() != null) {
            scheduleNextCharge(order.getUser(), order.getPlanType(), subscription.getBillingKey(), subscription.getExpiresAt());
        }

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

    // 가장 최근 결제완료 건 조회
    @Transactional
    public PaymentCompleteResponse getLatestPaidPayment(Long userId) {
        return paymentRepository.findFirstByOrder_User_IdAndStatusOrderByPaidAtDesc(userId, PaymentStatus.PAID)
                .map(payment -> PaymentCompleteResponse.of(payment.getPaymentId(), payment.getOrder().getPlanType(), payment.getStatus(), payment.getPaidAt()))
                .orElse(null);
    }

    // 결제 취소 (환불) - 본인 PAID 결제 건을 결제일로부터 CANCELLABLE_DAYS 이내에 취소
    @Transactional(noRollbackFor = CustomException.class)
    public PaymentCancelResponse cancelPayment(String paymentId, Long userId, String reason) {
        Payment payment = paymentRepository.findByPaymentIdForUpdate(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        Order order = payment.getOrder();

        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, "본인 결제만 취소할 수 있습니다.");
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new CustomException(ErrorCode.PAYMENT_NOT_CANCELLABLE, "결제 완료 상태 건만 취소할 수 있습니다.");
        }

        if (payment.getPaidAt() == null || payment.getPaidAt().isBefore(LocalDateTime.now().minusDays(CANCELLABLE_DAYS))) {
            throw new CustomException(ErrorCode.PAYMENT_NOT_CANCELLABLE, "결제일로부터 " + CANCELLABLE_DAYS + "일이 지나 취소할 수 없습니다.");
        }

        CancelPaymentResponse response;
        try {
            response = paymentClient.cancelPayment(
                    payment.getPaymentId(), null, null, null, reason,
                    CancelRequester.Customer.INSTANCE, null, null, null, null, null
            ).join();
        } catch (CompletionException e) {
            log.warn("PortOne 결제 취소 실패: paymentId={}, cause={}", payment.getPaymentId(), e.getCause() != null ? e.getCause().toString() : e.toString());
            throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }

        LocalDateTime cancelledAt = extractCancelledAt(response);
        payment.cancel(cancelledAt, reason);
        order.cancel();

        cancelSubscriptionAndSchedule(order.getUser());

        return PaymentCancelResponse.of(payment.getPaymentId(), payment.getStatus(), cancelledAt, reason);
    }

    private LocalDateTime extractCancelledAt(CancelPaymentResponse response) {
        if (response.getCancellation() instanceof PaymentCancellation.Recognized recognized) {
            return LocalDateTime.ofInstant(recognized.getCancelledAt(), ZoneId.systemDefault());
        }
        return LocalDateTime.now();
    }

    // 구독 즉시 해지 + 예약된 다음 회차 결제 취소
    private void cancelSubscriptionAndSchedule(User user) {
        subscriptionService.cancelIfActive(user.getId())
                .map(Subscription::getBillingKey)
                .ifPresent(billingKey -> revokeNextSchedule(user.getId(), billingKey, "구독 해지로 인한 예약 결제 취소"));
    }

    // 회원 탈퇴 - 구독 해지 + 다음 회차 예약 취소 + 빌링키 폐기
    @Transactional
    public void cancelSubscriptionForWithdrawal(Long userId) {
        subscriptionService.cancelIfActive(userId)
                .map(Subscription::getBillingKey)
                .ifPresent(billingKey -> {
                    revokeNextSchedule(userId, billingKey, "회원 탈퇴로 인한 예약 결제 취소");

                    try {
                        billingKeyClient.deleteBillingKey(billingKey, "회원 탈퇴", BillingKeyDeleteRequester.Customer.INSTANCE, null)
                                .join();
                    } catch (CompletionException e) {
                        log.warn("빌링키 폐기 실패: userId={}, cause={}", userId, e.getCause() != null ? e.getCause().toString() : e.toString());
                    }
                });
    }

    // 계정 정지 - 다음 회차 예약만 취소
    @Transactional
    public void revokeNextChargeForSuspension(Long userId) {
        subscriptionService.getActiveBillingKey(userId)
                .ifPresent(billingKey -> revokeNextSchedule(userId, billingKey, "계정 정지로 인한 예약 결제 취소"));
    }

    // 예약된 다음 회차 결제(PortOne 예약 + 로컬 레코드) 취소
    private void revokeNextSchedule(Long userId, String billingKey, String cancelReason) {
        paymentRepository.findFirstByOrder_User_IdAndStatusOrderByCreatedAtDesc(userId, PaymentStatus.READY)
                .ifPresent(nextPayment -> {
                    try {
                        paymentScheduleClient.revokePaymentSchedules(billingKey, List.of(nextPayment.getPaymentId()))
                                .join();
                    } catch (CompletionException e) {
                        log.warn("다음 정기결제 예약 취소 실패: userId={}, cause={}", userId, e.getCause() != null ? e.getCause().toString() : e.toString());
                    }
                    nextPayment.cancel(LocalDateTime.now(), cancelReason);
                    nextPayment.getOrder().cancel();
                });
    }
}
