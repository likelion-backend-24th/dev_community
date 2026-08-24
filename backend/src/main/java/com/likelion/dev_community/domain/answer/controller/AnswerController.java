package com.likelion.dev_community.domain.answer.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.answer.dto.AnswerRequest;
import com.likelion.dev_community.domain.answer.dto.AnswerResponse;
import com.likelion.dev_community.domain.answer.service.AnswerService;
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

@Tag(name = "답변", description = "질문에 대한 답변의 등록/조회/수정/삭제와 채택/채택취소를 다루는 API")
@RestController
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    // F-12
    @Operation(summary = "답변 등록", description = "특정 질문에 답변을 작성. 로그인 필요.")
    @PostMapping("/api/questions/{questionId}/answers")
    public ResponseEntity<ApiResponse<AnswerResponse>> createAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerRequest request
    ) {
        AnswerResponse response = answerService.createAnswer(userDetails.getId(), userDetails.isAdmin(), questionId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("답변 등록 완료", response));
    }

    // 답변 목록 조회
    @Operation(summary = "답변 목록 조회", description = "특정 질문에 달린 답변 전체 목록을 조회. 인증 불필요.")
    @GetMapping("/api/questions/{questionId}/answers")
    public ResponseEntity<ApiResponse<List<AnswerResponse>>> readAnswers(
            @PathVariable Long questionId
    ) {
        List<AnswerResponse> response = answerService.readAnswers(questionId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 답변 단건 조회
    @Operation(summary = "답변 단건 조회", description = "답변 ID로 답변 1건을 조회. 인증 불필요.")
    @GetMapping("/api/answers/{answerId}")
    public ResponseEntity<ApiResponse<AnswerResponse>> readAnswer(
            @PathVariable Long answerId
    ) {
        AnswerResponse response = answerService.getAnswer(answerId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // F-13
    @Operation(summary = "답변 수정", description = "본인이 작성한 답변을 수정. ADMIN은 타인 답변도 수정 가능.")
    @PatchMapping("/api/answers/{answerId}")
    public ResponseEntity<ApiResponse<AnswerResponse>> updateAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long answerId,
            @Valid @RequestBody AnswerRequest request
    ) {
        boolean isAdmin = userDetails.isAdmin();

        AnswerResponse response = answerService.updateAnswer(userDetails.getId(), answerId, isAdmin, request);

        return ResponseEntity.ok(ApiResponse.success("답변 수정 완료", response));
    }

    // F-13
    @Operation(summary = "답변 삭제", description = "본인이 작성한 답변을 soft delete 처리. ADMIN은 타인 답변도 삭제 가능.")
    @DeleteMapping("/api/answers/{answerId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long answerId
    ) {
        boolean isAdmin = userDetails.isAdmin();

        answerService.deleteAnswer(userDetails.getId(), answerId, isAdmin);

        return ResponseEntity.ok(ApiResponse.success("답변 삭제 완료", null));
    }

    // F-14 채택
    @Operation(summary = "답변 채택", description = "질문 작성자가 자신의 질문에 달린 답변 하나를 채택. 답변자의 평판 점수가 반영됨.")
    @PostMapping("/api/answers/{answerId}/adopt")
    public ResponseEntity<ApiResponse<AnswerResponse>> adoptAnswer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long answerId
    ) {
        AnswerResponse response = answerService.adoptAnswer(userDetails.getId(), answerId);

        return ResponseEntity.ok(ApiResponse.success("답변 채택 완료", response));
    }

    // F-14-1 채택 취소
    @Operation(summary = "답변 채택 취소", description = "질문 작성자가 이미 채택한 답변의 채택을 취소.")
    @DeleteMapping("/api/answers/{answerId}/adopt")
    public ResponseEntity<ApiResponse<AnswerResponse>> cancelAdoption(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long answerId
    ) {
        AnswerResponse response = answerService.cancelAdoption(userDetails.getId(), answerId);

        return ResponseEntity.ok(ApiResponse.success("답변 채택 취소 완료", response));
    }

}
