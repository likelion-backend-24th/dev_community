package com.likelion.dev_community.domain.question.repository;

import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionSortType;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import com.likelion.dev_community.domain.question.entity.QuestionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuestionRepositoryCustom {

    // F-17, F-18, F-19 조합
    Page<Question> searchQuestions(
            String keyword,
            Long tagId,
            QuestionStatus status,
            QuestionSortType sortType,
            Pageable pageable
    );

    // F-32 프리미엄 게시판용. type은 글 유형 필터(null이면 전체).
    Page<Question> searchPremiumQuestions(
            String keyword,
            Long tagId,
            QuestionStatus status,
            QuestionType type,
            QuestionSortType sortType,
            Pageable pageable
    );
}
