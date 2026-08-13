package com.likelion.dev_community.domain.payment.repository;

import com.likelion.dev_community.domain.payment.entity.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByUserId(Long userId);
}
