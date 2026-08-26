package com.likelion.dev_community.domain.question.entity;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;

public enum QuestionSortType {
    LATEST, LIKE, UNRESOLVED, VIEW;

    public static QuestionSortType from(String sort) {
        if (sort == null || sort.isBlank()) {
            return LATEST;
        }
        try {
            return QuestionSortType.valueOf(sort.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "정렬 조건이 올바르지 않습니다.");
        }
    }
}