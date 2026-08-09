package com.likelion.dev_community.domain.question.service;

import com.likelion.dev_community.domain.question.dto.*;
import org.springframework.data.domain.Page;

public interface QuestionService {

    // F-06
    QuestionResponse createQuestion(Long userId, QuestionCreateRequest request);

    // F-07
    Page<QuestionSummaryResponse> readQuestions(int page, int size, String sort, String keyword, String tag, String status);

    // F-08
    QuestionDetailResponse readDetailQuestion(Long questionId, String viewerKey);

    // F-09 (수정)
    QuestionResponse updateQuestion(Long questionId, Long userId, boolean isAdmin, QuestionUpdateRequest request);

    // F-09 (삭제)
    void deleteQuestion(Long questionId, Long userId, boolean isAdmin);
}
