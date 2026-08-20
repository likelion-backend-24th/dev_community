package com.likelion.dev_community.domain.chat.entity;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // 답변자(채팅을 건 사람). 질문자는 question.getAuthor()로 알 수 있어 별도 컬럼을 두지 않는다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answerer_id", nullable = false)
    private User answerer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomStatus status;

    // 참여자별로 마지막에 이 방을 읽은 시각. null이면 한 번도 읽지 않은 것으로 간주한다.
    private LocalDateTime questionerReadAt;
    private LocalDateTime answererReadAt;

    @Builder
    public ChatRoom(Question question, User answerer) {
        this.question = question;
        this.answerer = answerer;
        this.status = ChatRoomStatus.PENDING;
    }

    public void accept() {
        this.status = ChatRoomStatus.ACTIVE;
    }

    public void adopt() {
        this.status = ChatRoomStatus.ADOPTED;
    }

    public void close() {
        this.status = ChatRoomStatus.CLOSED;
    }

    public void markReadByQuestioner(LocalDateTime now) {
        this.questionerReadAt = now;
    }

    public void markReadByAnswerer(LocalDateTime now) {
        this.answererReadAt = now;
    }
}
