package com.likelion.dev_community.domain.dashboard.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.dashboard.dto.ActivityTimelineItem;
import com.likelion.dev_community.domain.dashboard.dto.PersonalDashboardSummaryResponse;
import com.likelion.dev_community.domain.dashboard.service.PersonalDashboardService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 인가는 SecurityConfig의 anyRequest().authenticated() 규칙으로 처리됨 (로그인한 회원 본인 데이터만 조회)
@Tag(name = "개인 대시보드", description = "로그인한 회원 본인의 활동 요약(질문/답변/채택/평판)과 최근 활동 타임라인을 조회하는 API. 구독 여부 무관 전체 회원 기본 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/me/dashboard")
public class PersonalDashboardController {

    private final PersonalDashboardService personalDashboardService;

    @Operation(summary = "개인 활동 요약 조회", description = "본인의 질문 수, 답변 수, 채택된 답변 수, 미해결 질문 수, 평판 점수를 조회.")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PersonalDashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "개인 활동 요약 조회 성공", personalDashboardService.getSummary(userDetails.getId())));
    }

    @Operation(summary = "개인 활동 타임라인 조회", description = "본인이 작성한 질문/답변을 최근 순으로 섞어 타임라인 형태로 조회.")
    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<List<ActivityTimelineItem>>> getTimeline(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "최근 활동 타임라인 조회 성공", personalDashboardService.getTimeline(userDetails.getId())));
    }
}
