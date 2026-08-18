package com.likelion.dev_community.domain.dashboard.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.dashboard.dto.ActivityTimelineItem;
import com.likelion.dev_community.domain.dashboard.dto.PersonalDashboardSummaryResponse;
import com.likelion.dev_community.domain.dashboard.service.PersonalDashboardService;
import com.likelion.dev_community.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 인가는 SecurityConfig의 anyRequest().authenticated() 규칙으로 처리됨 (로그인한 회원 본인 데이터만 조회)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/me/dashboard")
public class PersonalDashboardController {

    private final PersonalDashboardService personalDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PersonalDashboardSummaryResponse>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "개인 활동 요약 조회 성공", personalDashboardService.getSummary(userDetails.getId())));
    }

    @GetMapping("/timeline")
    public ResponseEntity<ApiResponse<List<ActivityTimelineItem>>> getTimeline(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "최근 활동 타임라인 조회 성공", personalDashboardService.getTimeline(userDetails.getId())));
    }
}
