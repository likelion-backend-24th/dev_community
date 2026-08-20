package com.likelion.dev_community.domain.chat.service;

import com.likelion.dev_community.common.AuthorizationValidator;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.chat.dto.*;
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
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final ReputationService reputationService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    // 답변자가 커리어상담 글에 1:1 채팅을 개설 (최초 메시지와 함께)
    public ChatRoomDetailResponse openChat(Long userId, boolean isAdmin, Long questionId, ChatMessageRequest request) {
        subscriptionService.requireActiveSubscriber(userId, isAdmin);

        User answerer = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "질문을 찾을 수 없습니다."));

        if (question.getType() != QuestionType.CAREER_CONSULT) {
            throw new CustomException(ErrorCode.CHAT_NOT_ALLOWED);
        }
        if (question.getAuthor().getId().equals(userId)) {
            throw new CustomException(ErrorCode.CHAT_SELF_NOT_ALLOWED);
        }
        if (question.getStatus() == QuestionStatus.RESOLVED) {
            throw new CustomException(ErrorCode.QUESTION_ALREADY_RESOLVED);
        }

        // 이미 열어둔 채팅방이 있으면 그대로 반환 (중복 개설 방지)
        ChatRoom chatRoom = chatRoomRepository.findByQuestionIdAndAnswererId(questionId, userId)
                .orElseGet(() -> {
                    ChatRoom created = chatRoomRepository.save(
                            ChatRoom.builder().question(question).answerer(answerer).build()
                    );
                    question.lockType();
                    chatMessageRepository.save(
                            ChatMessage.builder().chatRoom(created).sender(answerer).content(request.content()).build()
                    );
                    notificationService.notifyNewChatRoom(created);
                    return created;
                });

        return toDetail(chatRoom, userId);
    }

    // 내가 질문자 또는 답변자로 참여 중인 채팅방 목록
    @Transactional(readOnly = true)
    public List<ChatRoomListItemResponse> getMyChatRooms(Long userId) {
        return chatRoomRepository.findAllByParticipant(userId).stream()
                .map(room -> ChatRoomListItemResponse.of(room, userId, isUnread(room, userId)))
                .toList();
    }

    // 우하단 '내 채팅' 버튼에 표시할, 안읽은 메시지가 있는 채팅방 개수
    @Transactional(readOnly = true)
    public long getUnreadRoomCount(Long userId) {
        return chatRoomRepository.findAllByParticipant(userId).stream()
                .filter(room -> isUnread(room, userId))
                .count();
    }

    public ChatRoomDetailResponse getChatRoom(Long userId, Long roomId) {
        ChatRoom chatRoom = findRoomAndValidateAccess(roomId, userId);
        markRead(chatRoom, userId);
        return toDetail(chatRoom, userId);
    }

    // 채팅방을 이미 열어둔 상태에서 실시간으로 새 메시지를 받았을 때, 그 메시지도 곧바로 읽음 처리하기 위한 엔드포인트
    public void markRoomRead(Long userId, Long roomId) {
        ChatRoom chatRoom = findRoomAndValidateAccess(roomId, userId);
        markRead(chatRoom, userId);
    }

    public ChatMessageResponse sendMessage(Long userId, Long roomId, ChatMessageRequest request) {
        ChatRoom chatRoom = findRoomAndValidateAccess(roomId, userId);

        if (chatRoom.getStatus() == ChatRoomStatus.ADOPTED || chatRoom.getStatus() == ChatRoomStatus.CLOSED) {
            throw new CustomException(ErrorCode.CHAT_NOT_ACTIVE);
        }

        boolean isAnswerer = chatRoom.getAnswerer().getId().equals(userId);

        if (chatRoom.getStatus() == ChatRoomStatus.PENDING) {
            if (!isAnswerer) {
                throw new CustomException(ErrorCode.CHAT_NOT_ACTIVE, "채팅을 수락한 뒤 메시지를 보낼 수 있습니다.");
            }
            if (chatMessageRepository.countByChatRoomIdAndSenderId(roomId, userId) >= 1) {
                throw new CustomException(ErrorCode.CHAT_FIRST_MESSAGE_LIMIT);
            }
        }

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));

        ChatMessage message = chatMessageRepository.save(
                ChatMessage.builder().chatRoom(chatRoom).sender(sender).content(request.content()).build()
        );

        ChatMessageResponse response = ChatMessageResponse.from(message);

        User recipient = isAnswerer ? chatRoom.getQuestion().getAuthor() : chatRoom.getAnswerer();
        messagingTemplate.convertAndSendToUser(recipient.getUsername(), "/queue/chat-messages", response);

        return response;
    }

    // 질문자가 최초 1회 채팅을 수락 -> 이후 자유롭게 대화 가능
    public ChatRoomDetailResponse acceptChat(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        AuthorizationValidator.validateAuthor(chatRoom.getQuestion().getAuthor().getId(), userId, "질문 작성자만 채팅을 수락할 수 있습니다.");

        if (chatRoom.getStatus() != ChatRoomStatus.PENDING) {
            throw new CustomException(ErrorCode.CHAT_ALREADY_ACCEPTED);
        }

        chatRoom.accept();
        reputationService.apply(chatRoom.getAnswerer().getId(), ReputationEvent.CHAT_ACCEPTED);
        notificationService.notifyChatAccepted(chatRoom);
        broadcastRoomUpdate(chatRoom);

        return toDetail(chatRoom, userId);
    }

    // 질문자가 채택 -> 질문 해결 처리 + 답변자 평판 지급 + 나머지 채팅방 종료
    public ChatRoomDetailResponse adoptChat(Long userId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        Question question = chatRoom.getQuestion();

        AuthorizationValidator.validateAuthor(question.getAuthor().getId(), userId, "질문 작성자만 채팅을 채택할 수 있습니다.");

        if (chatRoom.getStatus() != ChatRoomStatus.ACTIVE) {
            throw new CustomException(ErrorCode.CHAT_NOT_ACCEPTED);
        }
        if (question.getStatus() == QuestionStatus.RESOLVED) {
            throw new CustomException(ErrorCode.QUESTION_ALREADY_RESOLVED);
        }

        chatRoom.adopt();
        question.resolve();
        reputationService.apply(chatRoom.getAnswerer().getId(), ReputationEvent.CHAT_ADOPTED);
        notificationService.notifyChatAdopted(chatRoom);
        broadcastRoomUpdate(chatRoom);

        LocalDateTime now = LocalDateTime.now();
        chatRoomRepository.findByQuestionId(question.getId()).stream()
                .filter(room -> !room.getId().equals(chatRoom.getId()))
                .forEach(room -> {
                    // 질문자가 하나를 읽고 채택했다면, 미처 열어보지 않은 나머지 채팅도 함께 읽음 처리한다.
                    room.markReadByQuestioner(now);
                    if (room.getStatus() == ChatRoomStatus.PENDING || room.getStatus() == ChatRoomStatus.ACTIVE) {
                        room.close();
                        broadcastRoomUpdate(room);
                    }
                });

        return toDetail(chatRoom, userId);
    }

    private ChatRoom findRoomAndValidateAccess(Long roomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        boolean isParticipant = chatRoom.getAnswerer().getId().equals(userId)
                || chatRoom.getQuestion().getAuthor().getId().equals(userId);
        if (!isParticipant) {
            throw new CustomException(ErrorCode.CHAT_FORBIDDEN);
        }

        return chatRoom;
    }

    private ChatRoomDetailResponse toDetail(ChatRoom chatRoom, Long viewerId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoom.getId());
        return ChatRoomDetailResponse.of(chatRoom, viewerId, messages);
    }

    private void markRead(ChatRoom chatRoom, Long viewerId) {
        LocalDateTime now = LocalDateTime.now();
        if (chatRoom.getQuestion().getAuthor().getId().equals(viewerId)) {
            chatRoom.markReadByQuestioner(now);
        } else {
            chatRoom.markReadByAnswerer(now);
        }
    }

    private boolean isUnread(ChatRoom chatRoom, Long viewerId) {
        boolean isQuestioner = chatRoom.getQuestion().getAuthor().getId().equals(viewerId);
        LocalDateTime readAt = isQuestioner ? chatRoom.getQuestionerReadAt() : chatRoom.getAnswererReadAt();

        if (readAt == null) {
            return chatMessageRepository.existsByChatRoomIdAndSenderIdNot(chatRoom.getId(), viewerId);
        }
        return chatMessageRepository.existsByChatRoomIdAndSenderIdNotAndCreatedAtAfter(chatRoom.getId(), viewerId, readAt);
    }

    private void broadcastRoomUpdate(ChatRoom chatRoom) {
        ChatRoomUpdatePayload payload = new ChatRoomUpdatePayload(chatRoom.getId(), chatRoom.getStatus().name());
        messagingTemplate.convertAndSendToUser(chatRoom.getAnswerer().getUsername(), "/queue/chat-room-updates", payload);
        messagingTemplate.convertAndSendToUser(chatRoom.getQuestion().getAuthor().getUsername(), "/queue/chat-room-updates", payload);
    }
}
