package com.likelion.dev_community.domain.chat.repository;

import com.likelion.dev_community.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByQuestionIdAndAnswererId(Long questionId, Long answererId);

    List<ChatRoom> findByQuestionId(Long questionId);

    // 질문자 또는 답변자로 참여 중인 모든 채팅방 (내 채팅 목록)
    @Query("select cr from ChatRoom cr where cr.question.author.id = :userId or cr.answerer.id = :userId order by cr.id desc")
    List<ChatRoom> findAllByParticipant(@Param("userId") Long userId);
}
