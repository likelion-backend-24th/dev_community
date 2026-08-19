package com.likelion.dev_community.domain.question.dto;

import com.likelion.dev_community.domain.question.entity.CodeComment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CodeCommentResponse {

    private final Long id;
    private final Long questionId;
    private final Long authorId;
    private final String authorNickname;
    private final int lineNumber;
    private final String content;
    private final LocalDateTime createdAt;

    public static CodeCommentResponse from(CodeComment comment) {
        return new CodeCommentResponse(
                comment.getId(),
                comment.getQuestion().getId(),
                comment.getAuthor().getId(),
                comment.getAuthor().getDisplayNickname(),
                comment.getLineNumber(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
