package com.likelion.dev_community.domain.chat.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.chat.dto.ChatMessageRequest;
import com.likelion.dev_community.domain.chat.dto.ChatMessageResponse;
import com.likelion.dev_community.domain.chat.dto.ChatRoomDetailResponse;
import com.likelion.dev_community.domain.chat.dto.ChatRoomListItemResponse;
import com.likelion.dev_community.domain.chat.service.ChatService;
import com.likelion.dev_community.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 답변자가 커리어상담 글에 1:1 채팅 개설
    @PostMapping("/api/questions/{questionId}/chat-rooms")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> openChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long questionId,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        ChatRoomDetailResponse response = chatService.openChat(userDetails.getId(), userDetails.isAdmin(), questionId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("채팅 개설 완료", response));
    }

    // 내 채팅 목록 (질문자/답변자 겸용)
    @GetMapping("/api/chat-rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomListItemResponse>>> getMyChatRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ChatRoomListItemResponse> response = chatService.getMyChatRooms(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 우하단 '내 채팅' 버튼 뱃지에 표시할, 안읽은 메시지가 있는 채팅방 개수
    @GetMapping("/api/chat-rooms/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadChatRoomCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        long count = chatService.getUnreadRoomCount(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/api/chat-rooms/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> getChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ChatRoomDetailResponse response = chatService.getChatRoom(userDetails.getId(), roomId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/api/chat-rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        ChatMessageResponse response = chatService.sendMessage(userDetails.getId(), roomId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    // 질문자가 최초 1회 채팅 수락
    @PatchMapping("/api/chat-rooms/{roomId}/accept")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> acceptChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ChatRoomDetailResponse response = chatService.acceptChat(userDetails.getId(), roomId);

        return ResponseEntity.ok(ApiResponse.success("채팅 수락 완료", response));
    }

    // 질문자가 답변(채팅) 채택
    @PatchMapping("/api/chat-rooms/{roomId}/adopt")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> adoptChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ChatRoomDetailResponse response = chatService.adoptChat(userDetails.getId(), roomId);

        return ResponseEntity.ok(ApiResponse.success("채팅 채택 완료", response));
    }
}
