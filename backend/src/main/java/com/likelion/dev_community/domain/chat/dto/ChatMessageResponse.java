package com.likelion.dev_community.domain.chat.dto;

import com.likelion.dev_community.domain.chat.entity.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "5") Long chatRoomId,
        @Schema(example = "3") Long senderId,
        @Schema(example = "BE24-Team4") String senderNickname,
        @Schema(example = "안녕하세요, 편하신 시간에 답변 부탁드립니다.") String content,
        @Schema(example = "2026-08-23T10:00:00") LocalDateTime createdAt
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
