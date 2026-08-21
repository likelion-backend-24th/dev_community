package com.likelion.dev_community.domain.answer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerRequest {
    @NotBlank(message = "내용을 입력해주세요.")
    private final String content;

    private final boolean isAnonymous;
}
