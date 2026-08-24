package com.likelion.dev_community.domain.subscription.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.subscription.dto.SubscriptionResponse;
import com.likelion.dev_community.domain.subscription.service.SubscriptionService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "구독", description = "로그인한 회원 본인의 구독(프리미엄 플랜) 정보 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "내 구독 정보 조회", description = "본인의 현재 구독 상태(플랜, 시작일, 만료일)를 조회. 구독 이력이 없으면 data가 null로 응답됨.")
    @GetMapping("/me/subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        SubscriptionResponse mySubscription = subscriptionService.getMySubscription(customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("구독 여부 조회 성공",mySubscription));
    }
}
