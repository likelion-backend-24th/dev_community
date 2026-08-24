package com.likelion.dev_community.domain.chat.dto;

import com.likelion.dev_community.domain.chat.entity.ChatRoom;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatRoomListItemResponse(
        @Schema(example = "1", description = "채팅방 ID") Long id,
        @Schema(example = "10", description = "커리어상담 질문 ID") Long questionId,
        @Schema(example = "커리어 상담 요청드립니다", description = "질문 제목") String questionTitle,
        @Schema(example = "QUESTIONER", description = "조회하는 본인의 역할. QUESTIONER 또는 ANSWERER") String role, // QUESTIONER or ANSWERER (viewer 기준)
        @Schema(example = "BE24-Team4", description = "상대방(본인이 질문자면 답변자, 답변자면 질문자) 닉네임") String counterpartNickname,
        @Schema(example = "120", description = "상대방 평판 점수") int counterpartReputation,
        @Schema(example = "true", description = "상대방 전문가 인증 여부") boolean counterpartIsExpert,
        @Schema(example = "ACTIVE", description = "채팅방 상태. PENDING, ACTIVE, ADOPTED, CLOSED 중 하나") String status,
        @Schema(example = "2026-08-23T10:00:00", description = "채팅방 개설일시") LocalDateTime createdAt,
        @Schema(example = "true", description = "본인 기준 안읽은 메시지 존재 여부") boolean unread
) {
    public static ChatRoomListItemResponse of(ChatRoom room, Long viewerId, boolean unread) {
        Question question = room.getQuestion();
        boolean isQuestioner = question.getAuthor().getId().equals(viewerId);
        User counterpart = isQuestioner ? room.getAnswerer() : question.getAuthor();

        return new ChatRoomListItemResponse(
                room.getId(),
                question.getId(),
                question.getTitle(),
                isQuestioner ? "QUESTIONER" : "ANSWERER",
                counterpart.getDisplayNickname(),
                counterpart.getReputation(),
                counterpart.isExpert(),
                room.getStatus().name(),
                room.getCreatedAt(),
                unread
        );
    }
}
