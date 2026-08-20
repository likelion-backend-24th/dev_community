package com.likelion.dev_community.domain.question.repository;

import com.likelion.dev_community.domain.question.entity.QuestionTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface QuestionTagRepository extends JpaRepository<QuestionTag, Long> {

    // 여러 질문의 태그를 한 번에 가져오기 (등록 순서 보존)
    @EntityGraph(attributePaths = "tag")
    List<QuestionTag> findByQuestionIdInOrderBySortOrderAsc(List<Long> questionIds);
}
