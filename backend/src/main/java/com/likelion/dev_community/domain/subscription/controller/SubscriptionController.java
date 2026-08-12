package com.likelion.dev_community.domain.subscription.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.subscription.dto.SubscriptionResponse;
import com.likelion.dev_community.domain.subscription.service.SubscriptionService;
import com.likelion.dev_community.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/me/subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getMySubscription(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        SubscriptionResponse mySubscription = subscriptionService.getMySubscription(customUserDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("구독 여부 조회 성공",mySubscription));
    }
}
