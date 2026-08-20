package com.likelion.dev_community.domain.question.service;

import com.likelion.dev_community.domain.question.dto.*;
import org.springframework.data.domain.Page;

public interface QuestionService {

    // F-06
    QuestionResponse createQuestion(Long userId, boolean isAdmin, QuestionCreateRequest request);

    // F-07
    Page<QuestionSummaryResponse> readQuestions(int page, int size, String sort, String keyword, String tag, String status);

    // F-32 프리미엄 게시판
    Page<QuestionSummaryResponse> readPremiumQuestions(int page, int size, String sort, String keyword, String tag, String status, Long userId, boolean isAdmin);

    // F-08
    QuestionDetailResponse readDetailQuestion(Long questionId, String viewerKey, Long userId, boolean isAdmin);

    // F-09 (수정)
    QuestionResponse updateQuestion(Long questionId, Long userId, boolean isAdmin, QuestionUpdateRequest request);

    // F-09 (삭제)
    void deleteQuestion(Long questionId, Long userId, boolean isAdmin);

    // 질문 글 AI 요약 (멤버십 구독자 전용)
    String getSummary(Long questionId, Long userId, boolean isAdmin);
}
