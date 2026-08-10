package com.likelion.dev_community.domain.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionUpdateRequest {
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private final String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private final String content;

    private final List<String> tags;
}