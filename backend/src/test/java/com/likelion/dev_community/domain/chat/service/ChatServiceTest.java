package com.likelion.dev_community.domain.chat.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.chat.dto.ChatMessageRequest;
import com.likelion.dev_community.domain.chat.dto.ChatMessageResponse;
import com.likelion.dev_community.domain.chat.dto.ChatRoomDetailResponse;
import com.likelion.dev_community.domain.chat.dto.ChatRoomListItemResponse;
import com.likelion.dev_community.domain.chat.entity.ChatMessage;
import com.likelion.dev_community.domain.chat.entity.ChatRoom;
import com.likelion.dev_community.domain.chat.entity.ChatRoomStatus;
import com.likelion.dev_community.domain.chat.repository.ChatMessageRepository;
import com.likelion.dev_community.domain.chat.repository.ChatRoomRepository;
import com.likelion.dev_community.domain.notification.service.NotificationService;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import com.likelion.dev_community.domain.question.entity.QuestionType;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.reputation.entity.ReputationEvent;
import com.likelion.dev_community.domain.reputation.service.ReputationService;
import com.likelion.dev_community.domain.subscription.service.SubscriptionService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private ReputationService reputationService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(chatRoomRepository, chatMessageRepository, questionRepository, userRepository,
                subscriptionService, reputationService, notificationService, messagingTemplate);
    }

    // ===== openChat =====

    @Test
    void 정상적으로_채팅방을_개설한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatMessageRequest request = new ChatMessageRequest("상담 요청드립니다.");

        when(userRepository.findById(2L)).thenReturn(Optional.of(answerer));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(chatRoomRepository.findByQuestionIdAndAnswererId(10L, 2L)).thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom room = invocation.getArgument(0);
            setId(room, 100L);
            return room;
        });
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        ChatRoomDetailResponse response = chatService.openChat(2L, false, 10L, request);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.role()).isEqualTo("ANSWERER");
        assertThat(question.isTypeLocked()).isTrue();
        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(notificationService).notifyNewChatRoom(any(ChatRoom.class));
    }

    @Test
    void 이미_개설된_채팅방이_있으면_기존_방을_그대로_반환한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom existingRoom = createChatRoom(100L, question, answerer);
        ChatMessageRequest request = new ChatMessageRequest("두 번째 시도");

        when(userRepository.findById(2L)).thenReturn(Optional.of(answerer));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(chatRoomRepository.findByQuestionIdAndAnswererId(10L, 2L)).thenReturn(Optional.of(existingRoom));
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        ChatRoomDetailResponse response = chatService.openChat(2L, false, 10L, request);

        assertThat(response.id()).isEqualTo(100L);
        verify(chatRoomRepository, never()).save(any());
        verify(chatMessageRepository, never()).save(any());
        verify(notificationService, never()).notifyNewChatRoom(any());
    }

    @Test
    void 커리어상담이_아닌_질문에는_채팅을_개설할_수_없다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.GENERAL);
        ChatMessageRequest request = new ChatMessageRequest("상담 요청");

        when(userRepository.findById(2L)).thenReturn(Optional.of(answerer));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> chatService.openChat(2L, false, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CHAT_NOT_ALLOWED));
    }

    @Test
    void 본인_질문에는_채팅을_개설할_수_없다() {
        User asker = createUser(1L, "asker");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatMessageRequest request = new ChatMessageRequest("셀프 상담");

        when(userRepository.findById(1L)).thenReturn(Optional.of(asker));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> chatService.openChat(1L, false, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CHAT_SELF_NOT_ALLOWED));
    }

    @Test
    void 이미_해결된_질문에는_채팅을_개설할_수_없다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        question.resolve();
        ChatMessageRequest request = new ChatMessageRequest("상담 요청");

        when(userRepository.findById(2L)).thenReturn(Optional.of(answerer));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> chatService.openChat(2L, false, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.QUESTION_ALREADY_RESOLVED));
    }

    @Test
    void 존재하지_않는_사용자면_채팅_개설시_예외가_발생한다() {
        ChatMessageRequest request = new ChatMessageRequest("상담 요청");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.openChat(999L, false, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_질문이면_채팅_개설시_예외가_발생한다() {
        User answerer = createUser(2L, "answerer");
        ChatMessageRequest request = new ChatMessageRequest("상담 요청");

        when(userRepository.findById(2L)).thenReturn(Optional.of(answerer));
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.openChat(2L, false, 999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    // ===== getMyChatRooms / getUnreadRoomCount =====

    @Test
    void 내_채팅방_목록을_조회한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);

        when(chatRoomRepository.findAllByParticipant(1L)).thenReturn(List.of(room));
        when(chatMessageRepository.existsByChatRoomIdAndSenderIdNot(100L, 1L)).thenReturn(true);

        List<ChatRoomListItemResponse> responses = chatService.getMyChatRooms(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(100L);
        assertThat(responses.get(0).unread()).isTrue();
    }

    @Test
    void 안읽은_채팅방_개수를_조회한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom unreadRoom = createChatRoom(100L, question, answerer);
        ChatRoom readRoom = createChatRoom(101L, question, answerer);

        when(chatRoomRepository.findAllByParticipant(1L)).thenReturn(List.of(unreadRoom, readRoom));
        when(chatMessageRepository.existsByChatRoomIdAndSenderIdNot(100L, 1L)).thenReturn(true);
        when(chatMessageRepository.existsByChatRoomIdAndSenderIdNot(101L, 1L)).thenReturn(false);

        long count = chatService.getUnreadRoomCount(1L);

        assertThat(count).isEqualTo(1);
    }

    // ===== getChatRoom =====

    @Test
    void 참여자는_채팅방_상세를_조회하고_읽음_처리된다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        ChatRoomDetailResponse response = chatService.getChatRoom(1L, 100L);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.role()).isEqualTo("QUESTIONER");
        assertThat(room.getQuestionerReadAt()).isNotNull();
    }

    @Test
    void 참여자가_아니면_채팅방_조회시_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatService.getChatRoom(999L, 100L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CHAT_FORBIDDEN));
    }

    @Test
    void 존재하지_않는_채팅방_조회시_예외가_발생한다() {
        when(chatRoomRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getChatRoom(1L, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }

    // ===== sendMessage =====

    @Test
    void 활성_채팅방에서_메시지를_보내면_상대방에게_전송된다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);
        room.accept();
        ChatMessageRequest request = new ChatMessageRequest("안녕하세요");

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(asker));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            setId(message, 1000L);
            return message;
        });

        ChatMessageResponse response = chatService.sendMessage(1L, 100L, request);

        assertThat(response.content()).isEqualTo("안녕하세요");
        verify(messagingTemplate).convertAndSendToUser("answerer", "/queue/chat-messages", response);
    }

    @Test
    void PENDING_상태에서_질문자가_메시지를_보내면_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);
        ChatMessageRequest request = new ChatMessageRequest("아직 수락 전인데요");

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatService.sendMessage(1L, 100L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CHAT_NOT_ACTIVE));
    }

    @Test
    void PENDING_상태에서_답변자가_두번째_메시지를_보내면_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);
        ChatMessageRequest request = new ChatMessageRequest("한 번 더 보낼래요");

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));
        when(chatMessageRepository.countByChatRoomIdAndSenderId(100L, 2L)).thenReturn(1L);

        assertThatThrownBy(() -> chatService.sendMessage(2L, 100L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CHAT_FIRST_MESSAGE_LIMIT));
    }

    @Test
    void ADOPTED_상태의_채팅방에서는_메시지를_보낼_수_없다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);
        room.accept();
        room.adopt();
        ChatMessageRequest request = new ChatMessageRequest("채택 후 메시지");

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatService.sendMessage(1L, 100L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CHAT_NOT_ACTIVE));
    }

    @Test
    void CLOSED_상태의_채팅방에서는_메시지를_보낼_수_없다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);
        room.close();
        ChatMessageRequest request = new ChatMessageRequest("종료된 방에 메시지");

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatService.sendMessage(1L, 100L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CHAT_NOT_ACTIVE));
    }

    // ===== acceptChat =====

    @Test
    void 질문_작성자가_아니면_채팅_수락시_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatService.acceptChat(999L, 100L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void 이미_수락된_채팅방을_다시_수락하면_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);
        room.accept();

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatService.acceptChat(1L, 100L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CHAT_ALREADY_ACCEPTED));
    }

    @Test
    void 정상적으로_채팅을_수락한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        ChatRoomDetailResponse response = chatService.acceptChat(1L, 100L);

        assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.ACTIVE);
        assertThat(response.status()).isEqualTo("ACTIVE");
        verify(reputationService).apply(2L, ReputationEvent.CHAT_ACCEPTED);
        verify(notificationService).notifyChatAccepted(room);
    }

    // ===== adoptChat =====

    @Test
    void 질문_작성자가_아니면_채택시_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);
        room.accept();

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatService.adoptChat(999L, 100L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void ACTIVE_상태가_아니면_채택시_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        ChatRoom room = createChatRoom(100L, question, answerer);

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatService.adoptChat(1L, 100L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CHAT_NOT_ACCEPTED));
    }

    @Test
    void 이미_해결된_질문의_채팅방은_채택할_수_없다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);
        question.resolve();
        ChatRoom room = createChatRoom(100L, question, answerer);
        room.accept();

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> chatService.adoptChat(1L, 100L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.QUESTION_ALREADY_RESOLVED));
    }

    @Test
    void 채택하면_질문이_해결되고_형제_채팅방들이_종료된다() {
        User asker = createUser(1L, "asker");
        User answerer = createUser(2L, "answerer");
        User otherAnswerer = createUser(3L, "otherAnswerer");
        Question question = createQuestion(10L, asker, QuestionType.CAREER_CONSULT);

        ChatRoom room = createChatRoom(100L, question, answerer);
        room.accept();

        ChatRoom pendingSibling = createChatRoom(101L, question, otherAnswerer);
        ChatRoom activeSibling = createChatRoom(102L, question, otherAnswerer);
        activeSibling.accept();
        ChatRoom closedSibling = createChatRoom(103L, question, otherAnswerer);
        closedSibling.close();

        when(chatRoomRepository.findById(100L)).thenReturn(Optional.of(room));
        when(chatRoomRepository.findByQuestionId(10L)).thenReturn(List.of(room, pendingSibling, activeSibling, closedSibling));
        when(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(100L)).thenReturn(List.of());

        ChatRoomDetailResponse response = chatService.adoptChat(1L, 100L);

        assertThat(question.getStatus()).isEqualTo(QuestionStatus.RESOLVED);
        assertThat(room.getStatus()).isEqualTo(ChatRoomStatus.ADOPTED);
        assertThat(response.status()).isEqualTo("ADOPTED");
        assertThat(pendingSibling.getStatus()).isEqualTo(ChatRoomStatus.CLOSED);
        assertThat(activeSibling.getStatus()).isEqualTo(ChatRoomStatus.CLOSED);
        assertThat(room.getStatus()).isNotEqualTo(pendingSibling.getStatus());
        assertThat(pendingSibling.getQuestionerReadAt()).isNotNull();
        verify(reputationService).apply(2L, ReputationEvent.CHAT_ADOPTED);
        verify(notificationService).notifyChatAdopted(room);
    }

    // ===== 헬퍼 =====

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

    private ChatRoom createChatRoom(Long id, Question question, User answerer) {
        ChatRoom room = ChatRoom.builder().question(question).answerer(answerer).build();
        setId(room, id);
        return room;
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
