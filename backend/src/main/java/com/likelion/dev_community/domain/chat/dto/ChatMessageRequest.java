package com.likelion.dev_community.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank(message = "메시지 내용을 입력해주세요.")
        String content
) {
}
