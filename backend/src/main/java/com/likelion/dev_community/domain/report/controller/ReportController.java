package com.likelion.dev_community.domain.report.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.report.dto.ReportRequest;
import com.likelion.dev_community.domain.report.dto.ReportResponse;
import com.likelion.dev_community.domain.report.service.ReportService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "신고", description = "질문/답변에 대한 신고 접수 API. 접수된 신고 처리는 관리자 API(/api/admin/reports)에서 다룸")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    // 신고 접수
    @Operation(summary = "신고 접수", description = "질문 또는 답변을 신고. 로그인 필요.")
    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                    @Valid @RequestBody ReportRequest reportRequest){
        ReportResponse reportResponse = reportService.report(customUserDetails.getId(), reportRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("신고 접수 완료",reportResponse));
    }
}
