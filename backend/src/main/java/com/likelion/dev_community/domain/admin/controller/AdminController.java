package com.likelion.dev_community.domain.admin.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.report.dto.ReportProcessRequest;
import com.likelion.dev_community.domain.report.dto.ReportResponse;
import com.likelion.dev_community.domain.report.entity.ReportStatus;
import com.likelion.dev_community.domain.report.service.ReportService;
import com.likelion.dev_community.domain.user.dto.userDto.UserInfoResponse;
import com.likelion.dev_community.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final ReportService reportService;
    private final UserService userService;

    // 신고 목록 조회
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getReports(@RequestParam(required = false) ReportStatus status,
                                                                        @ParameterObject @PageableDefault(size = 10) Pageable pageable){
        Page<ReportResponse> reports = reportService.getReports(status, pageable);

        Map<String, Object> meta = Map.of(
                "totalElements", reports.getTotalElements(),
                "totalPages", reports.getTotalPages(),
                "page", reports.getNumber(),
                "size", reports.getSize()
        );

        return ResponseEntity.ok(ApiResponse.success("신고 목록 조회 성공", reports.getContent(), meta));
    }

    // 신고 개별 처리
    @PatchMapping("/reports/{id}")
    public ResponseEntity<ApiResponse<ReportResponse>> processingReport(@PathVariable(name = "id") Long reportId,
                                                                        @Valid @RequestBody ReportProcessRequest request){
        ReportResponse reportResponse = reportService.processReport(reportId, request);

        return ResponseEntity.ok(ApiResponse.success(reportId + "신고 처리 성공",reportResponse));
    }

    // 회원 목록 전체 조회
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserInfoResponse>>> getAllUsers(@ParameterObject @PageableDefault(size = 10) Pageable pageable){

        Page<UserInfoResponse> allUsersInfo = userService.getAllUsersInfo(pageable);

        Map<String, Object> meta = Map.of(
                "totalElements", allUsersInfo.getTotalElements(),
                "totalPages", allUsersInfo.getTotalPages(),
                "page", allUsersInfo.getNumber(),
                "size", allUsersInfo.getSize()
        );

        return ResponseEntity.ok(ApiResponse.success("전체 회원 목록 조회 성공", allUsersInfo.getContent(), meta));
    }

    // 특정 유저의 누적된 신고 목록 카운트
    @GetMapping("/users/{id}/reports")
    public ResponseEntity<ApiResponse<Long>> countReportByTargetUserId(@PathVariable(name = "id") Long userId){
        userService.findUserById(userId);
        Long count = reportService.countByTargetUserId(userId);

        return ResponseEntity.ok(ApiResponse.success("유저 신고 누적 카운트 조회 성공",count));
    }

    // 관리자가 특정 유저 정지 수행
    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<UserInfoResponse>> userSuspension(@PathVariable(name = "id") Long userId){
        UserInfoResponse userInfoResponse = userService.userSuspension(userId);

        return ResponseEntity.ok(ApiResponse.success(userId + " 회원 정지됨", userInfoResponse));
    }

    // 정지 해제
    @PatchMapping("/users/{id}/unsuspend")
    public ResponseEntity<ApiResponse<UserInfoResponse>> userUnsuspension(@PathVariable(name = "id") Long userId){
        UserInfoResponse userInfoResponse = userService.userUnsuspension(userId);

        return ResponseEntity.ok(ApiResponse.success(userId + " 회원 정지 해제", userInfoResponse));
    }

    // 관리자가 특정 유저를 전문가로 승인
    @PatchMapping("/users/{id}/expert")
    public ResponseEntity<ApiResponse<UserInfoResponse>> grantExpert(@PathVariable(name = "id") Long userId){
        UserInfoResponse userInfoResponse = userService.grantExpert(userId);

        return ResponseEntity.ok(ApiResponse.success(userId + " 전문가로 지정됨", userInfoResponse));
    }

    // 전문가 지정 해제
    @DeleteMapping("/users/{id}/expert")
    public ResponseEntity<ApiResponse<UserInfoResponse>> revokeExpert(@PathVariable(name = "id") Long userId){
        UserInfoResponse userInfoResponse = userService.revokeExpert(userId);

        return ResponseEntity.ok(ApiResponse.success(userId + " 전문가 지정 해제", userInfoResponse));
    }
}
