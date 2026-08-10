package com.likelion.dev_community.domain.answer.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.answer.dto.AnswerRequest;
import com.likelion.dev_community.domain.answer.dto.AnswerResponse;
import com.likelion.dev_community.domain.answer.service.AnswerService;
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
public class AnswerController {

    private final AnswerService answerService;

    // F-12
    @PostMapping("/api/questions/{questionId}/answers")
    public ResponseEntity<ApiResponse<AnswerResponse>> createAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request
    ) {
        AnswerResponse response = answerService.createAnswer(userDetails.getId(), questionId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("답변 등록 완료", response));
    }

    // 답변 목록 조회
    @GetMapping("/api/questions/{questionId}/answers")
    public ResponseEntity<ApiResponse<List<AnswerResponse>>> readAnswers(
            @PathVariable Long questionId
    ) {
        List<AnswerResponse> response = answerService.readAnswers(questionId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // F-13
    @PatchMapping("/api/answers/{answerId}")
    public ResponseEntity<ApiResponse<AnswerResponse>> updateAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long answerId,
            @Valid @RequestBody AnswerRequest request
    ) {
        boolean isAdmin = isAdmin(userDetails);

        AnswerResponse response = answerService.updateAnswer(userDetails.getId(), answerId, isAdmin, request);

        return ResponseEntity.ok(ApiResponse.success("답변 수정 완료", response));
    }

    // F-13
    @DeleteMapping("/api/answers/{answerId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long answerId
    ) {
        boolean isAdmin = isAdmin(userDetails);

        answerService.deleteAnswer(userDetails.getId(), answerId, isAdmin);

        return ResponseEntity.ok(ApiResponse.success("답변 삭제 완료", null));
    }

    // F-14 채택
    @PostMapping("/api/answers/{answerId}/adopt")
    public ResponseEntity<ApiResponse<AnswerResponse>> adoptAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long answerId
    ) {
        AnswerResponse response = answerService.adoptAnswer(userDetails.getId(), answerId);

        return ResponseEntity.ok(ApiResponse.success("답변 채택 완료", response));
    }

    // F-14-1 채택 취소
    @DeleteMapping("/api/answers/{answerId}/adopt")
    public ResponseEntity<ApiResponse<AnswerResponse>> cancelAdoption(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long answerId
    ) {
        AnswerResponse response = answerService.cancelAdoption(userDetails.getId(), answerId);

        return ResponseEntity.ok(ApiResponse.success("답변 채택 취소 완료", response));
    }

    // 토큰 권한에 ROLE_ADMIN 있는지 확인
    private boolean isAdmin(CustomUserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
