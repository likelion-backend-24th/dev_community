package com.likelion.dev_community.domain.payment.repository;

import com.likelion.dev_community.domain.payment.entity.payment.Payment;
import com.likelion.dev_community.domain.payment.entity.payment.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);

    // 결제 완료 검증 시 동시 요청으로 인한 중복 처리를 막기 위한 락 조회
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.paymentId = :paymentId")
    Optional<Payment> findByPaymentIdForUpdate(@Param("paymentId") String paymentId);

    // 정기결제 취소 시 예약된 다음 회차 결제 건 조회
    Optional<Payment> findFirstByOrder_User_IdAndStatusOrderByCreatedAtDesc(Long userId, PaymentStatus status);

    // 마이페이지에서 취소 가능한(가장 최근 결제완료) 건 조회
    Optional<Payment> findFirstByOrder_User_IdAndStatusOrderByPaidAtDesc(Long userId, PaymentStatus status);

    // 예약 시각이 지났는데도 웹훅 유실 등으로 상태가 갱신되지 않은 결제건 조회 (재조회 스케줄러용)
    @Query("select p.paymentId from Payment p where p.status = :status and p.scheduledAt < :cutoff")
    List<String> findPaymentIdsByStatusAndScheduledAtBefore(@Param("status") PaymentStatus status, @Param("cutoff") LocalDateTime cutoff);
}
