package com.likelion.dev_community.domain.admin.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.answer.dto.AnswerResponse;
import com.likelion.dev_community.domain.dashboard.dto.ActivityTimelineItem;
import com.likelion.dev_community.domain.dashboard.dto.PersonalDashboardSummaryResponse;
import com.likelion.dev_community.domain.dashboard.service.PersonalDashboardService;
import com.likelion.dev_community.domain.question.dto.QuestionSummaryResponse;
import com.likelion.dev_community.domain.report.dto.ReportProcessRequest;
import com.likelion.dev_community.domain.report.dto.ReportResponse;
import com.likelion.dev_community.domain.report.entity.ReportStatus;
import com.likelion.dev_community.domain.report.service.ReportService;
import com.likelion.dev_community.domain.user.dto.userDto.UserInfoResponse;
import com.likelion.dev_community.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "관리자")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final ReportService reportService;
    private final UserService userService;
    private final PersonalDashboardService personalDashboardService;

    // 신고 목록 조회
    @Operation(summary = "신고 목록 조회")
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
    @Operation(summary = "신고 개별 처리")
    @PatchMapping("/reports/{id}")
    public ResponseEntity<ApiResponse<ReportResponse>> processingReport(@PathVariable(name = "id") Long reportId,
                                                                        @Valid @RequestBody ReportProcessRequest request){
        ReportResponse reportResponse = reportService.processReport(reportId, request);

        return ResponseEntity.ok(ApiResponse.success(reportId + "신고 처리 성공",reportResponse));
    }

    // 회원 목록 전체 조회 (expertRequested=true면 전문가 등급 요청한 유저만)
    @Operation(summary = "회원 목록 전체 조회")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserInfoResponse>>> getAllUsers(@RequestParam(required = false) Boolean expertRequested,
                                                                            @ParameterObject @PageableDefault(size = 10) Pageable pageable){

        Page<UserInfoResponse> allUsersInfo = userService.getAllUsersInfo(pageable, expertRequested);

        Map<String, Object> meta = Map.of(
                "totalElements", allUsersInfo.getTotalElements(),
                "totalPages", allUsersInfo.getTotalPages(),
                "page", allUsersInfo.getNumber(),
                "size", allUsersInfo.getSize()
        );

        return ResponseEntity.ok(ApiResponse.success("전체 회원 목록 조회 성공", allUsersInfo.getContent(), meta));
    }

    // 특정 유저의 누적된 신고 목록 카운트
    @Operation(summary = "유저 신고 누적 카운트 조회")
    @GetMapping("/users/{id}/reports")
    public ResponseEntity<ApiResponse<Long>> countReportByTargetUserId(@PathVariable(name = "id") Long userId){
        userService.findUserById(userId);
        Long count = reportService.countByTargetUserId(userId);

        return ResponseEntity.ok(ApiResponse.success("유저 신고 누적 카운트 조회 성공",count));
    }

    // 관리자가 특정 유저 정지 수행
    @Operation(summary = "회원 정지")
    @PatchMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<UserInfoResponse>> userSuspension(@PathVariable(name = "id") Long userId){
        UserInfoResponse userInfoResponse = userService.userSuspension(userId);

        return ResponseEntity.ok(ApiResponse.success(userId + " 회원 정지됨", userInfoResponse));
    }

    // 정지 해제
    @Operation(summary = "회원 정지 해제")
    @PatchMapping("/users/{id}/unsuspend")
    public ResponseEntity<ApiResponse<UserInfoResponse>> userUnsuspension(@PathVariable(name = "id") Long userId){
        UserInfoResponse userInfoResponse = userService.userUnsuspension(userId);

        return ResponseEntity.ok(ApiResponse.success(userId + " 회원 정지 해제", userInfoResponse));
    }

    // 관리자가 특정 유저를 전문가로 승인
    @Operation(summary = "전문가 승인")
    @PatchMapping("/users/{id}/expert")
    public ResponseEntity<ApiResponse<UserInfoResponse>> grantExpert(@PathVariable(name = "id") Long userId){
        UserInfoResponse userInfoResponse = userService.grantExpert(userId);

        return ResponseEntity.ok(ApiResponse.success(userId + " 전문가로 지정됨", userInfoResponse));
    }

    // 전문가 지정 해제
    @Operation(summary = "전문가 지정 해제")
    @DeleteMapping("/users/{id}/expert")
    public ResponseEntity<ApiResponse<UserInfoResponse>> revokeExpert(@PathVariable(name = "id") Long userId){
        UserInfoResponse userInfoResponse = userService.revokeExpert(userId);

        return ResponseEntity.ok(ApiResponse.success(userId + " 전문가 지정 해제", userInfoResponse));
    }

    // 전문가 등급 신청 거절
    @Operation(summary = "전문가 등급 신청 거절")
    @PostMapping("/users/{id}/expert-request/reject")
    public ResponseEntity<ApiResponse<UserInfoResponse>> rejectExpertRequest(@PathVariable(name = "id") Long userId){
        UserInfoResponse userInfoResponse = userService.rejectExpertRequest(userId);

        return ResponseEntity.ok(ApiResponse.success(userId + " 전문가 등급 요청 거절", userInfoResponse));
    }

    // 관리자가 특정 유저의 활동 대시보드 조회 (전문가 등급 요청 심사용)
    @Operation(summary = "특정 유저 활동 요약 조회")
    @GetMapping("/users/{id}/dashboard/summary")
    public ResponseEntity<ApiResponse<PersonalDashboardSummaryResponse>> getUserDashboardSummary(@PathVariable(name = "id") Long userId){
        userService.findUserById(userId);

        return ResponseEntity.ok(ApiResponse.success("유저 활동 요약 조회 성공", personalDashboardService.getSummary(userId)));
    }

    @Operation(summary = "특정 유저 활동 타임라인 조회")
    @GetMapping("/users/{id}/dashboard/timeline")
    public ResponseEntity<ApiResponse<List<ActivityTimelineItem>>> getUserDashboardTimeline(@PathVariable(name = "id") Long userId){
        userService.findUserById(userId);

        return ResponseEntity.ok(ApiResponse.success("유저 활동 타임라인 조회 성공", personalDashboardService.getTimeline(userId)));
    }

    // 관리자가 특정 유저의 질문/답변 목록 조회 (활동 대시보드 드릴다운용)
    @Operation(summary = "특정 유저 질문 목록 조회")
    @GetMapping("/users/{id}/questions")
    public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>> getUserQuestions(@PathVariable(name = "id") Long userId,
                                                                                        @ParameterObject @PageableDefault(size = 10) Pageable pageable){
        Page<QuestionSummaryResponse> questions = userService.getMyQuestions(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success("유저 질문 목록 조회 성공", questions.getContent()));
    }

    @Operation(summary = "특정 유저 답변 목록 조회")
    @GetMapping("/users/{id}/answers")
    public ResponseEntity<ApiResponse<List<AnswerResponse>>> getUserAnswers(@PathVariable(name = "id") Long userId,
                                                                             @ParameterObject @PageableDefault(size = 10) Pageable pageable){
        Page<AnswerResponse> answers = userService.getMyAnswers(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success("유저 답변 목록 조회 성공", answers.getContent()));
    }
}
