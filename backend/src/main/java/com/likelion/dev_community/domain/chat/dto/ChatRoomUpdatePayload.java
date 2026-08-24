package com.likelion.dev_community.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;

// 채팅방 상태(수락/채택/종료)가 바뀌었을 때 실시간으로 상대방에게 알려주기 위한 STOMP payload
public record ChatRoomUpdatePayload(
        @Schema(example = "1", description = "상태가 바뀐 채팅방 ID") Long roomId,
        @Schema(example = "ACTIVE", description = "변경된 채팅방 상태. PENDING, ACTIVE, ADOPTED, CLOSED 중 하나") String status
) {
}
