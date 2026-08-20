package com.likelion.dev_community.domain.chat.dto;

import com.likelion.dev_community.domain.chat.entity.ChatRoom;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.user.entity.User;

import java.time.LocalDateTime;

public record ChatRoomListItemResponse(
        Long id,
        Long questionId,
        String questionTitle,
        String role, // QUESTIONER or ANSWERER (viewer 기준)
        String counterpartNickname,
        int counterpartReputation,
        boolean counterpartIsExpert,
        String status,
        LocalDateTime createdAt,
        boolean unread
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
