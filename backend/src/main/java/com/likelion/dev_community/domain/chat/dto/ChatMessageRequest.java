package com.likelion.dev_community.domain.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank(message = "메시지 내용을 입력해주세요.")
        @Schema(example = "안녕하세요, 편하신 시간에 답변 부탁드립니다.")
        String content
) {
}
