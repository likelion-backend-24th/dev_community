package com.likelion.dev_community.domain.payment.entity.payment;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import com.likelion.dev_community.domain.payment.entity.order.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity { // 결제 이력 저장 테이블

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, unique = true, length = 50)
    private String paymentId; // 고유 결제 번호 (PortOne paymentId)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    private String cancelReason;

    @Builder
    public Payment(Order order, String paymentId) {
        this.order = order;
        this.paymentId = paymentId;
        this.status = PaymentStatus.READY;
    }

    // 결제 시도 시 READY 상태로 생성
    public static Payment create(Order order, String paymentId) {
        return Payment.builder()
                .order(order)
                .paymentId(paymentId)
                .build();
    }

    public void markPaid(LocalDateTime paidAt) {
        this.status = PaymentStatus.PAID;
        this.paidAt = paidAt;
    }

    public void markFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void cancel(LocalDateTime cancelledAt, String cancelReason) {
        this.status = PaymentStatus.CANCELLED;
        this.cancelledAt = cancelledAt;
        this.cancelReason = cancelReason;
    }
}
