package com.likelion.dev_community.domain.question.entity;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;

public enum QuestionType {
    GENERAL, CODE_REVIEW, CAREER_CONSULT;

    public static QuestionType from(String type) {
        if (type == null || type.isBlank()) {
            return GENERAL;
        }
        try {
            return QuestionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "게시글 유형이 올바르지 않습니다.");
        }
    }
}
