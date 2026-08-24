package com.likelion.dev_community.domain.question.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.common.PageMetaMapper;
import com.likelion.dev_community.common.viewcount.ViewerKeyResolver;
import com.likelion.dev_community.domain.question.dto.*;
import com.likelion.dev_community.domain.question.service.QuestionService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "질문")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final ViewerKeyResolver viewerKeyResolver;

    // F-06
    @Operation(summary = "질문 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<QuestionResponse>> createQuestion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody QuestionCreateRequest request
    ) {
        boolean isAdmin = userDetails.isAdmin();

        QuestionResponse response = questionService.createQuestion(userDetails.getId(), isAdmin, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("질문 등록 완료", response));
    }

    // F-07
    @Operation(summary = "질문 목록 조회")
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

        return ResponseEntity.ok(ApiResponse.success("질문 목록 조회", result.getContent(), PageMetaMapper.of(result)));
    }

    // F-32
    @Operation(summary = "프리미엄 질문 목록 조회")
    @GetMapping("/premium")
    public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>> readPremiumQuestions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String status
    ) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        boolean isAdmin = (userDetails != null) && userDetails.isAdmin();

        Page<QuestionSummaryResponse> result = questionService.readPremiumQuestions(page, size, sort, keyword, tag, status, userId, isAdmin);

        return ResponseEntity.ok(ApiResponse.success("프리미엄 질문 목록 조회", result.getContent(), PageMetaMapper.of(result)));
    }

    // F-08
    @Operation(summary = "질문 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> readDetailQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        Long userId = (userDetails != null) ? userDetails.getId() : null;
        boolean isAdmin = (userDetails != null) && userDetails.isAdmin();
        String viewerKey = viewerKeyResolver.resolve(userId, request);

        QuestionDetailResponse response = questionService.readDetailQuestion(id, viewerKey, userId, isAdmin);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // F-09 (수정)
    @Operation(summary = "질문 수정")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>> updateQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody QuestionUpdateRequest request
    ) {
        boolean isAdmin = userDetails.isAdmin();

        QuestionResponse response = questionService.updateQuestion(id, userDetails.getId(), isAdmin, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // F-09 (삭제)
    @Operation(summary = "질문 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        boolean isAdmin = userDetails.isAdmin();

        questionService.deleteQuestion(id, userDetails.getId(), isAdmin);

        return ResponseEntity.noContent().build();
    }

    // 질문 글 AI 요약 (멤버십 구독자 전용)
    @Operation(summary = "질문 AI 요약 조회 (구독자 전용)")
    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<String>> getSummary(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String summary = questionService.getSummary(id, userDetails.getId(), userDetails.isAdmin());

        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // 질문 작성 중 본문 기준 AI 태그 추천 (멤버십 구독자 전용)
    @Operation(summary = "AI 태그 추천 (구독자 전용)")
    @PostMapping("/tags/suggest")
    public ResponseEntity<ApiResponse<List<String>>> suggestTags(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TagSuggestRequest request
    ) {
        List<String> tags = questionService.suggestTags(
                userDetails.getId(), userDetails.isAdmin(), request.getTitle(), request.getContent());

        return ResponseEntity.ok(ApiResponse.success(tags));
    }
}