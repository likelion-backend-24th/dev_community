package com.likelion.dev_community.domain.chat.dto;

import com.likelion.dev_community.domain.chat.entity.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        @Schema(example = "1", description = "메시지 ID") Long id,
        @Schema(example = "5", description = "이 메시지가 속한 채팅방 ID") Long chatRoomId,
        @Schema(example = "3", description = "발신자 회원 ID") Long senderId,
        @Schema(example = "BE24-Team4", description = "발신자 닉네임") String senderNickname,
        @Schema(example = "안녕하세요, 편하신 시간에 답변 부탁드립니다.", description = "메시지 본문") String content,
        @Schema(example = "2026-08-23T10:00:00", description = "메시지 전송일시") LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getSender().getDisplayNickname(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
