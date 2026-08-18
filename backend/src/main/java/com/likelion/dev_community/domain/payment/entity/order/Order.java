package com.likelion.dev_community.domain.payment.entity.order;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import com.likelion.dev_community.domain.subscription.entity.PlanType;
import com.likelion.dev_community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanType planType;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Builder
    public Order(User user, PlanType planType, Long amount, String currency) {
        this.user = user;
        this.planType = planType;
        this.amount = amount;
        this.currency = currency;
        this.status = OrderStatus.PENDING;
    }

    // 결제 준비 시 PENDING 상태로 생성
    public static Order create(User user, PlanType planType, Long amount, String currency) {
        return Order.builder()
                .user(user)
                .planType(planType)
                .amount(amount)
                .currency(currency)
                .build();
    }

    public void complete() {
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
}
