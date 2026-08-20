package com.likelion.dev_community.domain.chat.repository;

import com.likelion.dev_community.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);

    long countByChatRoomIdAndSenderId(Long chatRoomId, Long senderId);

    // 상대방이 보낸 메시지가 하나라도 있는지 (한 번도 읽지 않은 방 판정용)
    boolean existsByChatRoomIdAndSenderIdNot(Long chatRoomId, Long senderId);

    // 마지막으로 읽은 시각 이후에 상대방이 보낸 메시지가 있는지 (안읽음 판정용)
    boolean existsByChatRoomIdAndSenderIdNotAndCreatedAtAfter(Long chatRoomId, Long senderId, LocalDateTime after);
}
