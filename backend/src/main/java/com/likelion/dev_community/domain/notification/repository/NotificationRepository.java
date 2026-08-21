package com.likelion.dev_community.domain.notification.repository;

import com.likelion.dev_community.domain.notification.entity.Notification;
import com.likelion.dev_community.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop5ByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientAndIsReadFalse(User recipient);
}
