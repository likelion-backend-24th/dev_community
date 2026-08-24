package com.likelion.dev_community.domain.chat.dto;

import com.likelion.dev_community.domain.chat.entity.ChatMessage;
import com.likelion.dev_community.domain.chat.entity.ChatRoom;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ChatRoomDetailResponse(
        @Schema(example = "1", description = "채팅방 ID") Long id,
        @Schema(example = "10", description = "커리어상담 질문 ID") Long questionId,
        @Schema(example = "커리어 상담 요청드립니다", description = "질문 제목") String questionTitle,
        QuestionStatus questionStatus,
        @Schema(example = "QUESTIONER", description = "조회하는 본인의 역할. QUESTIONER 또는 ANSWERER") String role, // QUESTIONER or ANSWERER (viewer 기준)
        @Schema(example = "5", description = "질문 작성자 회원 ID") Long questionerId,
        @Schema(example = "질문왕", description = "질문 작성자 닉네임") String questionerNickname,
        @Schema(example = "3", description = "답변자(채팅 개설자) 회원 ID") Long answererId,
        @Schema(example = "BE24-Team4", description = "답변자 닉네임") String answererNickname,
        @Schema(example = "120", description = "답변자 평판 점수") int answererReputation,
        @Schema(example = "true", description = "답변자 전문가 인증 여부") boolean answererIsExpert,
        @Schema(example = "ACTIVE", description = "채팅방 상태. PENDING, ACTIVE, ADOPTED, CLOSED 중 하나") String status,
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
