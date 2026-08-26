package com.likelion.dev_community.domain.user.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.answer.dto.AnswerResponse;
import com.likelion.dev_community.domain.question.dto.QuestionSummaryResponse;
import com.likelion.dev_community.domain.user.dto.userDto.AvatarColorResponse;
import com.likelion.dev_community.domain.user.dto.userDto.UserInfoRequest;
import com.likelion.dev_community.domain.user.dto.userDto.UserInfoResponse;
import com.likelion.dev_community.domain.user.dto.userDto.UserPwRequest;
import com.likelion.dev_community.domain.user.dto.userDto.UserWithdrawRequest;
import com.likelion.dev_community.domain.user.service.UserService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "회원", description = "로그인한 회원 본인의 정보 조회/수정/탈퇴, 마이페이지(내 질문·답변 목록), 전문가 등급 신청을 다루는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "인증 토큰의 사용자 ID로 본인 정보를 조회.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        UserInfoResponse userInfo = userService.getUserInfo(customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("회원 정보 조회 성공",userInfo));
    }

    @Operation(summary = "내 정보 수정", description = "닉네임 등 본인 정보를 수정.")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                        @Valid @RequestBody UserInfoRequest userInfoRequest){
        UserInfoResponse userInfo = userService.updateUserInfo(userInfoRequest, customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("회원 정보 수정 성공",userInfo));
    }

    @Operation(summary = "비밀번호 변경", description = "본인 비밀번호를 변경.")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updateUserPassword(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                @Valid @RequestBody UserPwRequest userPwRequest,
                                                                HttpServletResponse httpServletResponse){
        userService.updateUserPassword(userPwRequest, customUserDetails.getId(),httpServletResponse);

        return ResponseEntity.ok(ApiResponse.success("비밀번호 변경 성공",null));
    }

    @Operation(summary = "회원 탈퇴", description = "본인 비밀번호 확인 후 계정을 탈퇴(soft delete) 처리. 실제 데이터를 삭제하지 않고 상태만 WITHDRAWN으로 전환.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> softDelete(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                        @Valid @RequestBody UserWithdrawRequest request,
                                                        HttpServletResponse httpServletResponse){
        userService.deleteUser(customUserDetails.getId(), request.getCurrentPassword(), httpServletResponse);

        return ResponseEntity.noContent().build();
    }

    // 마이페이지 - 내 질문 목록 조회
    @Operation(summary = "내 질문 목록 조회", description = "마이페이지에서 본인이 작성한 질문 목록을 페이지 단위로 조회.")
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
    @Operation(summary = "내 답변 목록 조회", description = "마이페이지에서 본인이 작성한 답변 목록을 페이지 단위로 조회.")
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
    @Operation(summary = "전문가 등급 신청", description = "전문가 등급 신청을 접수. 승인은 관리자가 별도로 처리하며, 승인 전까지는 신청 대기 상태로 표시됨.")
    @PostMapping("/me/expert-request")
    public ResponseEntity<ApiResponse<UserInfoResponse>> requestExpert(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        UserInfoResponse userInfo = userService.requestExpert(customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("전문가 등급 신청이 접수되었습니다", userInfo));
    }

    // 마이페이지 - 멤버십 아바타 색상 리롤 (구독자 전용, 결제 주기당 1회)
    @Operation(summary = "아바타 색상 리롤", description = "멤버십 구독자가 아바타 색을 무작위로 다시 뽑는다. 결제 갱신 주기당 1회만 가능하며, 이미 이번 주기에 뽑았다면 409를 반환한다.")
    @PostMapping("/me/avatar-color/reroll")
    public ResponseEntity<ApiResponse<AvatarColorResponse>> rerollAvatarColor(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        AvatarColorResponse response = userService.rerollAvatarColor(customUserDetails.getId(), customUserDetails.isAdmin());

        return ResponseEntity.ok(ApiResponse.success("아바타 색상이 변경되었습니다", response));
    }
}
