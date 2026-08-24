package com.likelion.dev_community.domain.chat.controller;

import com.likelion.dev_community.common.ApiResponse;
import com.likelion.dev_community.domain.chat.dto.ChatMessageRequest;
import com.likelion.dev_community.domain.chat.dto.ChatMessageResponse;
import com.likelion.dev_community.domain.chat.dto.ChatRoomDetailResponse;
import com.likelion.dev_community.domain.chat.dto.ChatRoomListItemResponse;
import com.likelion.dev_community.domain.chat.service.ChatService;
import com.likelion.dev_community.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "채팅", description = "커리어상담 유형 질문에 대한 1:1 채팅방 개설·조회·메시지 송수신·수락·채택을 다루는 API. 채팅방은 PENDING→ACTIVE→ADOPTED/CLOSED 상태로 전이됨")
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // 답변자가 커리어상담 글에 1:1 채팅 개설
    @Operation(summary = "채팅방 개설", description = "답변자가 커리어상담 유형 질문에 대해 1:1 채팅방을 PENDING 상태로 개설하며 첫 메시지를 함께 보냄.")
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
    @Operation(summary = "내 채팅방 목록 조회", description = "질문자·답변자 어느 쪽으로든 참여 중인 채팅방 목록을 조회.")
    @GetMapping("/api/chat-rooms")
    public ResponseEntity<ApiResponse<List<ChatRoomListItemResponse>>> getMyChatRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ChatRoomListItemResponse> response = chatService.getMyChatRooms(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 우하단 '내 채팅' 버튼 뱃지에 표시할, 안읽은 메시지가 있는 채팅방 개수
    @Operation(summary = "안읽은 채팅방 개수 조회", description = "안읽은 메시지가 있는 채팅방 개수를 조회. 헤더의 채팅 버튼 뱃지 표시용.")
    @GetMapping("/api/chat-rooms/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadChatRoomCount(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        long count = chatService.getUnreadRoomCount(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @Operation(summary = "채팅방 상세 조회", description = "채팅방 정보와 메시지 이력을 조회. 참여자(질문자/답변자) 본인만 조회 가능.")
    @GetMapping("/api/chat-rooms/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> getChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ChatRoomDetailResponse response = chatService.getChatRoom(userDetails.getId(), roomId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 채팅방을 이미 보고 있는 상태에서 실시간으로 새 메시지가 온 경우, 그 메시지도 곧바로 읽음 처리
    @Operation(summary = "채팅방 읽음 처리", description = "채팅방을 보고 있는 동안 실시간으로 온 새 메시지를 즉시 읽음 처리.")
    @PatchMapping("/api/chat-rooms/{roomId}/read")
    public ResponseEntity<ApiResponse<Void>> markChatRoomRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        chatService.markRoomRead(userDetails.getId(), roomId);

        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "채팅 메시지 전송", description = "채팅방에 메시지를 전송. 참여자(질문자/답변자) 본인만 가능.")
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
    @Operation(summary = "채팅 수락", description = "질문자가 개설된 채팅을 최초 1회 수락해 방 상태를 PENDING에서 ACTIVE로 전환. 답변자에게 평판 점수가 반영됨.")
    @PatchMapping("/api/chat-rooms/{roomId}/accept")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> acceptChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ChatRoomDetailResponse response = chatService.acceptChat(userDetails.getId(), roomId);

        return ResponseEntity.ok(ApiResponse.success("채팅 수락 완료", response));
    }

    // 질문자가 답변(채팅) 채택
    @Operation(summary = "채팅 채택", description = "질문자가 채팅 상담(답변)을 채택해 방 상태를 ADOPTED로 전환. 답변자에게 평판 점수가 반영되고 연결된 질문이 자동으로 해결 처리됨.")
    @PatchMapping("/api/chat-rooms/{roomId}/adopt")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> adoptChat(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId
    ) {
        ChatRoomDetailResponse response = chatService.adoptChat(userDetails.getId(), roomId);

        return ResponseEntity.ok(ApiResponse.success("채팅 채택 완료", response));
    }
}
