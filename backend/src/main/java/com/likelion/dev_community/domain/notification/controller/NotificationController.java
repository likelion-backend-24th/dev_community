package com.likelion.dev_community.domain.notification.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.notification.dto.NotificationPayload;
import com.likelion.dev_community.domain.notification.service.NotificationService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "알림", description = "로그인한 회원 본인의 알림 목록 조회 및 읽음 처리를 다루는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "최근 알림 목록 조회", description = "본인에게 온 최근 알림 목록을 조회. 새 답변, 답변 채택, 채팅 관련 알림 등이 포함됨.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationPayload>>> getRecentNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {

        List<NotificationPayload> response = notificationService.getRecentNotifications(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "본인의 안읽은 알림을 모두 읽음 처리.")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.markAllAsRead(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success());
    }
}
