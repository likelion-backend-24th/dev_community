package com.likelion.dev_community.domain.question.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.common.viewcount.ViewerKeyResolver;
import com.likelion.dev_community.domain.question.dto.*;
import com.likelion.dev_community.domain.question.service.QuestionService;
import com.likelion.dev_community.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final ViewerKeyResolver viewerKeyResolver;

    // F-06
    @PostMapping
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody QuestionCreateRequest request
    ) {
        QuestionResponse response = questionService.createQuestion(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("질문 등록 완료", response));
    }

    // F-07
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>> readQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword, // F-17
            @RequestParam(required = false) String tag, // F-18
            @RequestParam(required = false) String status // F-19

    ) {
        Page<QuestionSummaryResponse> result = questionService.readQuestions(page, size, sort, keyword, tag, status);

        Map<String, Object> meta = Map.of(
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success("질문 목록 조회", result.getContent(), meta));
    }

    // F-08
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> readDetailQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        String viewerKey = viewerKeyResolver.resolve(userId, request);

        QuestionDetailResponse response = questionService.readDetailQuestion(id, viewerKey);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // F-09 (수정)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody QuestionUpdateRequest request
    ) {
        boolean isAdmin = isAdmin(userDetails);

        QuestionResponse response = questionService.updateQuestion(id, userDetails.getId(), isAdmin, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // F-09 (삭제)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isAdmin = isAdmin(userDetails);

        questionService.deleteQuestion(id, userDetails.getId(), isAdmin);

        return ResponseEntity.noContent().build();
    }

    // 토큰 권한에 ROLE_ADMIN 있는지 확인
    private boolean isAdmin(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}