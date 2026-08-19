package com.likelion.dev_community.domain.question.repository;

import com.likelion.dev_community.domain.question.entity.CodeComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeCommentRepository extends JpaRepository<CodeComment, Long> {

    @EntityGraph(attributePaths = "author")
    List<CodeComment> findByQuestionIdOrderByLineNumberAscCreatedAtAsc(Long questionId);
}
