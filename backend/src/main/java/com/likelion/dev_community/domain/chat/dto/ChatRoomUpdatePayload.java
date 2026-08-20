package com.likelion.dev_community.domain.chat.dto;

// 채팅방 상태(수락/채택/종료)가 바뀌었을 때 실시간으로 상대방에게 알려주기 위한 STOMP payload
public record ChatRoomUpdatePayload(
        Long roomId,
        String status
) {
}
