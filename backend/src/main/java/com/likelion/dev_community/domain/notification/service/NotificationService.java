package com.likelion.dev_community.domain.notification.service;

import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.notification.dto.NotificationPayload;
import com.likelion.dev_community.domain.notification.entity.Notification;
import com.likelion.dev_community.domain.notification.entity.NotificationType;
import com.likelion.dev_community.domain.notification.repository.NotificationRepository;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 내 질문에 새 답변이 달렸을 때, 질문 작성자에게 알림
    public void notifyNewAnswer(Question question, Answer answer) {
        send(
                question.getAuthor(),
                NotificationType.NEW_ANSWER,
                answer.getAuthor().getNickname() + "님이 질문에 답변을 남겼어요.",
                "/questions/" + question.getId()
        );
    }

    // 내 답변이 채택되었을 때, 답변 작성자에게 알림
    public void notifyAnswerAdopted(Answer answer) {
        send(
                answer.getAuthor(),
                NotificationType.ANSWER_ADOPTED,
                "작성하신 답변이 채택되었어요.",
                "/questions/" + answer.getQuestion().getId()
        );
    }

    private void send(User recipient, NotificationType type, String message, String link) {
        Notification notification = notificationRepository.save(
                Notification.builder()
                        .recipient(recipient)
                        .type(type)
                        .message(message)
                        .link(link)
                        .build()
        );

        messagingTemplate.convertAndSendToUser(
                recipient.getUsername(),
                "/queue/notifications",
                NotificationPayload.from(notification)
        );
    }
}
