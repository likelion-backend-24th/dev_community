package com.likelion.dev_community.domain.question.dto;

import com.likelion.dev_community.domain.question.entity.CodeComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CodeCommentResponse {

    @Schema(example = "1", description = "코멘트 ID")
    private final Long id;
    @Schema(example = "10", description = "이 코멘트가 달린 질문 ID")
    private final Long questionId;
    @Schema(example = "3", description = "작성자 회원 ID")
    private final Long authorId;
    @Schema(example = "BE24-Team4", description = "작성자 닉네임")
    private final String authorNickname;
    @Schema(example = "12", description = "코멘트가 달린 코드 라인 번호")
    private final int lineNumber;
    @Schema(example = "이 부분은 null 체크가 필요해 보입니다.", description = "코멘트 내용")
    private final String content;
    @Schema(example = "2026-08-23T10:00:00", description = "작성일시")
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
