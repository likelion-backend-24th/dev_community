package com.likelion.dev_community.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank(message = "메시지 내용을 입력해주세요.")
        @Schema(example = "안녕하세요, 편하신 시간에 답변 부탁드립니다.", description = "메시지 본문. 채팅방 개설 시에는 첫 메시지로 사용됨")
        String content
) {
}
