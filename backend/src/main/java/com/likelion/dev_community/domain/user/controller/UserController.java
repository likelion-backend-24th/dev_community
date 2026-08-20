package com.likelion.dev_community.domain.user.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.answer.dto.AnswerResponse;
import com.likelion.dev_community.domain.question.dto.QuestionSummaryResponse;
import com.likelion.dev_community.domain.user.dto.userDto.UserInfoRequest;
import com.likelion.dev_community.domain.user.dto.userDto.UserInfoResponse;
import com.likelion.dev_community.domain.user.dto.userDto.UserPwRequest;
import com.likelion.dev_community.domain.user.dto.userDto.UserWithdrawRequest;
import com.likelion.dev_community.domain.user.service.UserService;
import com.likelion.dev_community.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        UserInfoResponse userInfo = userService.getUserInfo(customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회 성공",userInfo));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                        @Valid @RequestBody UserInfoRequest userInfoRequest){
        UserInfoResponse userInfo = userService.updateUserInfo(userInfoRequest, customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("회원 정보 수정 성공",userInfo));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updateUserPassword(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                @Valid @RequestBody UserPwRequest userPwRequest,
                                                                HttpServletResponse httpServletResponse){
        userService.updateUserPassword(userPwRequest, customUserDetails.getId(),httpServletResponse);

        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 성공",null));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> softDelete(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                        @Valid @RequestBody UserWithdrawRequest request,
                                                        HttpServletResponse httpServletResponse){
        userService.deleteUser(customUserDetails.getId(), request.getCurrentPassword(), httpServletResponse);

        return ResponseEntity.noContent().build();
    }

    // 마이페이지 - 내 질문 목록 조회
    @GetMapping("/me/questions")
    public ResponseEntity<ApiResponse<List<QuestionSummaryResponse>>> getMyQuestions(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                                     @ParameterObject @PageableDefault(size = 10) Pageable pageable){
        Page<QuestionSummaryResponse> myQuestions = userService.getMyQuestions(customUserDetails.getId(), pageable);

        Map<String, Object> meta = Map.of(
                "page", myQuestions.getNumber(),
                "size", myQuestions.getSize(),
                "totalElements", myQuestions.getTotalElements(),
                "totalPages", myQuestions.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success("내 질문 목록 조회 성공", myQuestions.getContent(),meta));
    }

    // 마이페이지 - 내 답변 목록 조회
    @GetMapping("/me/answers")
    public ResponseEntity<ApiResponse<List<AnswerResponse>>> getMyAnswers(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                           @ParameterObject @PageableDefault(size = 10) Pageable pageable){
        Page<AnswerResponse> myAnswers = userService.getMyAnswers(customUserDetails.getId(), pageable);

        Map<String, Object> meta = Map.of(
                "page", myAnswers.getNumber(),
                "size", myAnswers.getSize(),
                "totalElements", myAnswers.getTotalElements(),
                "totalPages", myAnswers.getTotalPages()
        );

        return ResponseEntity.ok(ApiResponse.success("내 답변 목록 조회 성공", myAnswers.getContent(), meta));
    }

    // 마이페이지 - 전문가 등급 신청
    @PostMapping("/me/expert-request")
    public ResponseEntity<ApiResponse<UserInfoResponse>> requestExpert(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        UserInfoResponse userInfo = userService.requestExpert(customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("전문가 등급 신청이 접수되었습니다", userInfo));
    }
}
