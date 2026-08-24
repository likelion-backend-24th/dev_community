package com.likelion.dev_community.domain.chat.dto;

import com.likelion.dev_community.domain.chat.entity.ChatRoom;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ChatRoomListItemResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "10") Long questionId,
        @Schema(example = "커리어 상담 요청드립니다") String questionTitle,
        @Schema(example = "QUESTIONER") String role, // QUESTIONER or ANSWERER (viewer 기준)
        @Schema(example = "BE24-Team4") String counterpartNickname,
        @Schema(example = "120") int counterpartReputation,
        @Schema(example = "true") boolean counterpartIsExpert,
        @Schema(example = "ACTIVE") String status,
        @Schema(example = "2026-08-23T10:00:00") LocalDateTime createdAt,
        @Schema(example = "true") boolean unread
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
