package com.likelion.dev_community.domain.question.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.question.dto.CodeCommentRequest;
import com.likelion.dev_community.domain.question.dto.CodeCommentResponse;
import com.likelion.dev_community.domain.question.dto.CodeCommentUpdateRequest;
import com.likelion.dev_community.domain.question.service.CodeCommentService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "코드리뷰 댓글", description = "코드리뷰 유형 질문의 특정 라인에 대한 댓글 등록/조회/수정/삭제를 다루는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions/{questionId}/code-comments")
public class CodeCommentController {

    private final CodeCommentService codeCommentService;

    @Operation(summary = "코드 코멘트 등록", description = "코드리뷰 유형 질문의 특정 라인에 코멘트를 남김. 코드리뷰 유형이 아닌 질문에 시도하면 400. 로그인 필요.")
    @PostMapping
    public ResponseEntity<ApiResponse<CodeCommentResponse>> createComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                          @PathVariable Long questionId,
                                                                          @Valid @RequestBody CodeCommentRequest request) {
        CodeCommentResponse response = codeCommentService.createComment(userDetails.getId(), userDetails.isAdmin(), questionId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("코드 코멘트 등록 완료", response));
    }

    @Operation(summary = "코드 코멘트 목록 조회", description = "특정 질문에 달린 코드 코멘트 목록을 조회. 인증 불필요.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CodeCommentResponse>>> readComments(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                               @PathVariable Long questionId) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        boolean isAdmin = (userDetails != null) && userDetails.isAdmin();

        List<CodeCommentResponse> response = codeCommentService.readComments(userId, isAdmin, questionId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "코드 코멘트 수정", description = "본인이 작성한 코드 코멘트를 수정. ADMIN은 타인 코멘트도 수정 가능.")
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CodeCommentResponse>> updateComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                          @PathVariable Long questionId,
                                                                          @PathVariable Long commentId,
                                                                          @Valid @RequestBody CodeCommentUpdateRequest request) {
        CodeCommentResponse response = codeCommentService.updateComment(userDetails.getId(), userDetails.isAdmin(), questionId, commentId, request);

        return ResponseEntity.ok(ApiResponse.success("코드 코멘트 수정 완료", response));
    }

    @Operation(summary = "코드 코멘트 삭제", description = "본인이 작성한 코드 코멘트를 삭제. ADMIN은 타인 코멘트도 삭제 가능.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable Long questionId,
                                              @PathVariable Long commentId) {
        codeCommentService.deleteComment(userDetails.getId(), userDetails.isAdmin(), questionId, commentId);

        return ResponseEntity.noContent().build();
    }
}
