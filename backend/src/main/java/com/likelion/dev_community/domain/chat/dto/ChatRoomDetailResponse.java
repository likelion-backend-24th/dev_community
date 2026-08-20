package com.likelion.dev_community.domain.chat.dto;

import com.likelion.dev_community.domain.chat.entity.ChatMessage;
import com.likelion.dev_community.domain.chat.entity.ChatRoom;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;

import java.util.List;

public record ChatRoomDetailResponse(
        Long id,
        Long questionId,
        String questionTitle,
        QuestionStatus questionStatus,
        String role, // QUESTIONER or ANSWERER (viewer 기준)
        Long questionerId,
        String questionerNickname,
        Long answererId,
        String answererNickname,
        int answererReputation,
        boolean answererIsExpert,
        String status,
        List<ChatMessageResponse> messages
) {
    public static ChatRoomDetailResponse of(ChatRoom room, Long viewerId, List<ChatMessage> messages) {
        Question question = room.getQuestion();
        boolean isQuestioner = question.getAuthor().getId().equals(viewerId);

        return new ChatRoomDetailResponse(
                room.getId(),
                question.getId(),
                question.getTitle(),
                question.getStatus(),
                isQuestioner ? "QUESTIONER" : "ANSWERER",
                question.getAuthor().getId(),
                question.getAuthor().getDisplayNickname(),
                room.getAnswerer().getId(),
                room.getAnswerer().getDisplayNickname(),
                room.getAnswerer().getReputation(),
                room.getAnswerer().isExpert(),
                room.getStatus().name(),
                messages.stream().map(ChatMessageResponse::from).toList()
        );
    }
}
