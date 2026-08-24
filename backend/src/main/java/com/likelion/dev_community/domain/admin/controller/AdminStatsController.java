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
@Tag(name = "관리자 통계")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    // 최근 30일 일별 가입자/질문 추이
    @Operation(summary = "일별 가입자/질문 추이 조회")
    @GetMapping("/daily-trend")
    public ResponseEntity<ApiResponse<List<DailyStatItem>>> getDailyTrend() {
        return ResponseEntity.ok(ApiResponse.success("일별 가입자/질문 추이 조회 성공", adminStatsService.getDailyTrend()));
    }

    // 질문 해결률
    @Operation(summary = "질문 해결률 조회")
    @GetMapping("/resolution-rate")
    public ResponseEntity<ApiResponse<ResolutionRateResponse>> getResolutionRate() {
        return ResponseEntity.ok(ApiResponse.success("질문 해결률 조회 성공", adminStatsService.getResolutionRate()));
    }

    // 7일 이상 방치된(미답변) 질문
    @Operation(summary = "방치된 질문 조회")
    @GetMapping("/stale-questions")
    public ResponseEntity<ApiResponse<StaleQuestionsResponse>> getStaleQuestions() {
        return ResponseEntity.ok(ApiResponse.success("방치된 질문 조회 성공", adminStatsService.getStaleQuestions()));
    }

    // 조회수 상위 Top 5 질문
    @Operation(summary = "인기 질문 Top 5 조회")
    @GetMapping("/top-questions")
    public ResponseEntity<ApiResponse<List<TopQuestionItem>>> getTopQuestions() {
        return ResponseEntity.ok(ApiResponse.success("인기 질문 조회 성공", adminStatsService.getTopQuestions()));
    }
}
