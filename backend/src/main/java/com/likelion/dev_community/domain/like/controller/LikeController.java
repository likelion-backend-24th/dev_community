package com.likelion.dev_community.domain.like.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.like.dto.LikeStatusResponse;
import com.likelion.dev_community.domain.like.entity.LikeTargetType;
import com.likelion.dev_community.domain.like.service.LikeService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "추천")
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "질문 추천 토글")
    @PostMapping("/api/questions/{id}/like")
    public ApiResponse<Boolean> toggleQuestionLike(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @PathVariable Long id) {
        boolean liked = likeService.toggleLike(userDetails.getId(), LikeTargetType.QUESTION, id);
        return ApiResponse.success(liked);
    }

    @Operation(summary = "답변 추천 토글")
    @PostMapping("/api/answers/{id}/like")
    public ApiResponse<Boolean> toggleAnswerLike(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @PathVariable Long id) {
        boolean liked = likeService.toggleLike(userDetails.getId(), LikeTargetType.ANSWER, id);
        return ApiResponse.success(liked);
    }

    // 질문 상세 화면에서 질문/답변에 대한 내 추천 여부를 한 번에 조회
    // GET /api/questions/**는 SecurityConfig에서 permitAll이라 비로그인 사용자도 호출 가능 -> 그 경우 전부 false로 응답
    @Operation(summary = "내 추천 여부 일괄 조회")
    @GetMapping("/api/questions/{id}/like-status")
    public ApiResponse<LikeStatusResponse> getLikeStatus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                          @PathVariable Long id,
                                                          @RequestParam(required = false) List<Long> answerIds) {
        if (userDetails == null) {
            return ApiResponse.success(new LikeStatusResponse(false, List.of()));
        }
        LikeStatusResponse status = likeService.getLikeStatus(
                userDetails.getId(), id, answerIds != null ? answerIds : List.of());
        return ApiResponse.success(status);
    }
}
