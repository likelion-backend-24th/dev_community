package com.likelion.dev_community.domain.notification.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.chat.entity.ChatRoom;
import com.likelion.dev_community.domain.notification.dto.NotificationPayload;
import com.likelion.dev_community.domain.notification.entity.Notification;
import com.likelion.dev_community.domain.notification.entity.NotificationType;
import com.likelion.dev_community.domain.notification.repository.NotificationRepository;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // 벨 드롭다운에 보여줄 최근 알림 5개
    @Transactional(readOnly = true)
    public List<NotificationPayload> getRecentNotifications(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));

        return notificationRepository.findTop5ByRecipientOrderByCreatedAtDesc(user).stream()
                .map(NotificationPayload::from)
                .toList();
    }

    // 벨 드롭다운 열람 시 알림 전부 읽음 처리
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));

        notificationRepository.findByRecipientAndIsReadFalse(user)
                .forEach(Notification::markAsRead);
    }

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

    // 커리어상담 질문에 답변자가 1:1 채팅을 열었을 때, 질문 작성자에게 알림
    public void notifyNewChatRoom(ChatRoom chatRoom) {
        send(
                chatRoom.getQuestion().getAuthor(),
                NotificationType.NEW_CHAT_ROOM,
                chatRoom.getAnswerer().getNickname() + "님이 1:1 채팅을 요청했어요.",
                "/chats/" + chatRoom.getId()
        );
    }

    // 내가 건 채팅이 수락되었을 때, 답변자에게 알림
    public void notifyChatAccepted(ChatRoom chatRoom) {
        send(
                chatRoom.getAnswerer(),
                NotificationType.CHAT_ACCEPTED,
                "채팅이 수락되었어요. 대화를 이어가 보세요.",
                "/chats/" + chatRoom.getId()
        );
    }

    // 내 채팅이 채택되었을 때, 답변자에게 알림
    public void notifyChatAdopted(ChatRoom chatRoom) {
        send(
                chatRoom.getAnswerer(),
                NotificationType.CHAT_ADOPTED,
                "1:1 채팅 답변이 채택되었어요.",
                "/chats/" + chatRoom.getId()
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
