package com.likelion.dev_community.domain.notification.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.notification.dto.NotificationPayload;
import com.likelion.dev_community.domain.notification.service.NotificationService;
import com.likelion.dev_community.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationPayload>>> getRecentNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {

        List<NotificationPayload> response = notificationService.getRecentNotifications(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.markAllAsRead(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success());
    }
}
