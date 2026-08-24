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
import com.likelion.dev_community.domain.question.entity.QuestionType;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository, messagingTemplate);
    }

    @Test
    void 정상적으로_최근_알림_목록을_조회한다() {
        User user = createUser(1L, "asker");
        Notification notification1 = createNotification(1L, user, NotificationType.NEW_ANSWER, "알림1");
        Notification notification2 = createNotification(2L, user, NotificationType.ANSWER_ADOPTED, "알림2");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.findTop5ByRecipientOrderByCreatedAtDesc(user))
                .thenReturn(List.of(notification1, notification2));

        List<NotificationPayload> payloads = notificationService.getRecentNotifications(1L);

        assertThat(payloads).hasSize(2);
        assertThat(payloads.get(0).id()).isEqualTo(1L);
        assertThat(payloads.get(0).message()).isEqualTo("알림1");
        assertThat(payloads.get(1).id()).isEqualTo(2L);
        assertThat(payloads.get(1).message()).isEqualTo("알림2");
    }

    @Test
    void 존재하지_않는_사용자의_알림_조회시_예외가_발생한다() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getRecentNotifications(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 정상적으로_모든_알림을_읽음_처리한다() {
        User user = createUser(1L, "asker");
        Notification notification1 = createNotification(1L, user, NotificationType.NEW_ANSWER, "알림1");
        Notification notification2 = createNotification(2L, user, NotificationType.ANSWER_ADOPTED, "알림2");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.findByRecipientAndIsReadFalse(user))
                .thenReturn(List.of(notification1, notification2));

        notificationService.markAllAsRead(1L);

        assertThat(notification1.isRead()).isTrue();
        assertThat(notification2.isRead()).isTrue();
    }

    @Test
    void 읽지_않은_알림이_없으면_아무일도_일어나지_않는다() {
        User user = createUser(1L, "asker");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(notificationRepository.findByRecipientAndIsReadFalse(user)).thenReturn(List.of());

        notificationService.markAllAsRead(1L);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_사용자의_알림_읽음처리시_예외가_발생한다() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAllAsRead(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 새_답변이_달리면_질문_작성자에게_알림을_보낸다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker);
        Answer answer = createAnswer(100L, question, answerer);

        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyNewAnswer(question, answer);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("asker"),
                eq("/queue/notifications"),
                any(NotificationPayload.class)
        );
    }

    @Test
    void 답변이_채택되면_답변_작성자에게_알림을_보낸다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker);
        Answer answer = createAnswer(100L, question, answerer);

        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyAnswerAdopted(answer);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("answerer"),
                eq("/queue/notifications"),
                any(NotificationPayload.class)
        );
    }

    @Test
    void 새_채팅방이_생기면_질문_작성자에게_알림을_보낸다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom chatRoom = createChatRoom(1000L, question, answerer);

        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyNewChatRoom(chatRoom);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("asker"),
                eq("/queue/notifications"),
                any(NotificationPayload.class)
        );
    }

    @Test
    void 채팅이_수락되면_답변자에게_알림을_보낸다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom chatRoom = createChatRoom(1000L, question, answerer);

        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyChatAccepted(chatRoom);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("answerer"),
                eq("/queue/notifications"),
                any(NotificationPayload.class)
        );
    }

    @Test
    void 채팅이_채택되면_답변자에게_알림을_보낸다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom chatRoom = createChatRoom(1000L, question, answerer);

        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        notificationService.notifyChatAdopted(chatRoom);

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(
                eq("answerer"),
                eq("/queue/notifications"),
                any(NotificationPayload.class)
        );
    }

    private User createUser(Long id, String nickname) {
        User user = User.builder()
                .username(nickname)
                .password("encoded-password")
                .nickname(nickname)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        setId(user, id);
        return user;
    }

    private Question createQuestion(Long id, User author) {
        return createQuestion(id, author, QuestionType.GENERAL);
    }

    private Question createQuestion(Long id, User author, QuestionType type) {
        Question question = Question.builder()
                .author(author)
                .title("제목")
                .content("내용")
                .isPremium(type != QuestionType.GENERAL)
                .type(type)
                .build();
        setId(question, id);
        return question;
    }

    private Answer createAnswer(Long id, Question question, User author) {
        Answer answer = Answer.builder()
                .question(question)
                .author(author)
                .content("답변 내용")
                .build();
        setId(answer, id);
        return answer;
    }

    private ChatRoom createChatRoom(Long id, Question question, User answerer) {
        ChatRoom chatRoom = ChatRoom.builder()
                .question(question)
                .answerer(answerer)
                .build();
        setId(chatRoom, id);
        return chatRoom;
    }

    private Notification createNotification(Long id, User recipient, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .link("/questions/1")
                .build();
        setId(notification, id);
        return notification;
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
