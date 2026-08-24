package com.likelion.dev_community.domain.chat.dto;

import com.likelion.dev_community.domain.chat.entity.ChatMessage;
import com.likelion.dev_community.domain.chat.entity.ChatRoom;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ChatRoomDetailResponse(
        @Schema(example = "1") Long id,
        @Schema(example = "10") Long questionId,
        @Schema(example = "커리어 상담 요청드립니다") String questionTitle,
        QuestionStatus questionStatus,
        @Schema(example = "QUESTIONER") String role, // QUESTIONER or ANSWERER (viewer 기준)
        @Schema(example = "5") Long questionerId,
        @Schema(example = "질문왕") String questionerNickname,
        @Schema(example = "3") Long answererId,
        @Schema(example = "BE24-Team4") String answererNickname,
        @Schema(example = "120") int answererReputation,
        @Schema(example = "true") boolean answererIsExpert,
        @Schema(example = "ACTIVE") String status,
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
