package com.likelion.dev_community.domain.payment.entity.transaction;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import com.likelion.dev_community.domain.payment.entity.payment.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payment_transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentTransaction extends BaseTimeEntity { // 같은 Payment 내 PG 승인 시도 기록

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, unique = true, length = 50)
    private String transactionId; // PortOne transactionId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 10)
    private String currency;

    private String failReason;

    private LocalDateTime approvedAt;

    @Builder
    public PaymentTransaction(Payment payment, String transactionId, TransactionStatus status,
                               Long amount, String currency, String failReason, LocalDateTime approvedAt) {
        this.payment = payment;
        this.transactionId = transactionId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.failReason = failReason;
        this.approvedAt = approvedAt;
    }

    public static PaymentTransaction succeed(Payment payment, String transactionId, Long amount,
                                              String currency, LocalDateTime approvedAt) {
        return PaymentTransaction.builder()
                .payment(payment)
                .transactionId(transactionId)
                .status(TransactionStatus.SUCCEEDED)
                .amount(amount)
                .currency(currency)
                .approvedAt(approvedAt)
                .build();
    }

    public static PaymentTransaction fail(Payment payment, String transactionId, Long amount,
                                           String currency, String failReason) {
        return PaymentTransaction.builder()
                .payment(payment)
                .transactionId(transactionId)
                .status(TransactionStatus.FAILED)
                .amount(amount)
                .currency(currency)
                .failReason(failReason)
                .build();
    }
}
