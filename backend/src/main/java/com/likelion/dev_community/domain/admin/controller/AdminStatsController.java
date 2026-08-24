package com.likelion.dev_community.domain.admin.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.admin.dto.stats.DailyStatItem;
import com.likelion.dev_community.domain.admin.dto.stats.ResolutionRateResponse;
import com.likelion.dev_community.domain.admin.dto.stats.StaleQuestionsResponse;
import com.likelion.dev_community.domain.admin.dto.stats.TopQuestionItem;
import com.likelion.dev_community.domain.admin.service.AdminStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// 인가는 SecurityConfig의 "/api/admin/**" -> hasRole("ADMIN") 규칙으로 처리됨
@Tag(name = "관리자 통계", description = "관리자 대시보드용 통계(일별 가입자/질문 추이, 질문 해결률, 방치된 질문, 인기 질문) 조회 API. 전부 ADMIN 권한 필요")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    // 최근 30일 일별 가입자/질문 추이
    @Operation(summary = "일별 가입자/질문 추이 조회", description = "최근 30일간 일별 신규 가입자 수와 질문 등록 수를 조회. ADMIN 전용.")
    @GetMapping("/daily-trend")
    public ResponseEntity<ApiResponse<List<DailyStatItem>>> getDailyTrend() {
        return ResponseEntity.ok(ApiResponse.success("일별 가입자/질문 추이 조회 성공", adminStatsService.getDailyTrend()));
    }

    // 질문 해결률
    @Operation(summary = "질문 해결률 조회", description = "전체 질문 대비 채택된 답변이 있는(해결된) 질문의 비율을 조회. ADMIN 전용.")
    @GetMapping("/resolution-rate")
    public ResponseEntity<ApiResponse<ResolutionRateResponse>> getResolutionRate() {
        return ResponseEntity.ok(ApiResponse.success("질문 해결률 조회 성공", adminStatsService.getResolutionRate()));
    }

    // 7일 이상 방치된(미답변) 질문
    @Operation(summary = "방치된 질문 조회", description = "등록된 지 7일 이상 지났는데 답변이 하나도 없는 질문 목록을 조회. ADMIN 전용.")
    @GetMapping("/stale-questions")
    public ResponseEntity<ApiResponse<StaleQuestionsResponse>> getStaleQuestions() {
        return ResponseEntity.ok(ApiResponse.success("방치된 질문 조회 성공", adminStatsService.getStaleQuestions()));
    }

    // 조회수 상위 Top 5 질문
    @Operation(summary = "인기 질문 Top 5 조회", description = "조회수 기준 상위 5개 질문을 조회. ADMIN 전용.")
    @GetMapping("/top-questions")
    public ResponseEntity<ApiResponse<List<TopQuestionItem>>> getTopQuestions() {
        return ResponseEntity.ok(ApiResponse.success("인기 질문 조회 성공", adminStatsService.getTopQuestions()));
    }
}
