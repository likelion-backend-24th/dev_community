package com.likelion.dev_community.domain.question.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class QuestionCreateRequest {
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private final String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private final String content;

    private final List<String> tags;

    private final boolean isPremium;

    // 프론트에서 isPremium을 아예 안 보내는 경우(일반 질문 작성) Jackson이
    // primitive boolean에 null을 매핑하려다 500 에러를 내던 문제 수정.
    // 생성자 파라미터를 Boolean(래퍼)으로 받아 missing/null을 안전하게 흡수한 뒤 false로 기본 처리
    // (@JsonProperty의 defaultValue는 이 프로젝트의 Jackson 버전에서 creator 파라미터 생략 시 적용되지 않음)
    @JsonCreator
    public QuestionCreateRequest(
            @JsonProperty("title") String title,
            @JsonProperty("content") String content,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("isPremium") Boolean isPremium
    ) {
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.isPremium = Boolean.TRUE.equals(isPremium);
    }
}