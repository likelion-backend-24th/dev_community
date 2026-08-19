package com.likelion.dev_community.domain.question.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.question.dto.CodeCommentRequest;
import com.likelion.dev_community.domain.question.dto.CodeCommentResponse;
import com.likelion.dev_community.domain.question.service.CodeCommentService;
import com.likelion.dev_community.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions/{questionId}/code-comments")
public class CodeCommentController {

    private final CodeCommentService codeCommentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CodeCommentResponse>> createComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                          @PathVariable Long questionId,
                                                                          @Valid @RequestBody CodeCommentRequest request) {
        CodeCommentResponse response = codeCommentService.createComment(userDetails.getId(), userDetails.isAdmin(), questionId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("코드 코멘트 등록 완료", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CodeCommentResponse>>> readComments(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                               @PathVariable Long questionId) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        boolean isAdmin = (userDetails != null) && userDetails.isAdmin();

        List<CodeCommentResponse> response = codeCommentService.readComments(userId, isAdmin, questionId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
