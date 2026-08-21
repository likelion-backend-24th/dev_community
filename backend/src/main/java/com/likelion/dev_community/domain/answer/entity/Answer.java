package com.likelion.dev_community.domain.answer.entity;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "answers")
@SQLRestriction("deleted_at is null")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Answer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isAdopted;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private boolean isAnonymous;

    private LocalDateTime deletedAt;

    @Builder
    public Answer(Question question, User author, String content, boolean isAnonymous) {
        this.question = question;
        this.author = author;
        this.content = content;
        this.isAdopted = false;
        this.likeCount = 0;
        this.isAnonymous = isAnonymous;
    }

    public void update(String content) {
        this.content = content;
    }

    public void softDelete() {
        if (this.isAdopted) {
            throw new CustomException(ErrorCode.ADOPTED_ANSWER_DELETE_FORBIDDEN);
        }
        this.deletedAt = LocalDateTime.now();
    }

    // 질문 삭제에 연쇄되는 삭제라 채택 여부와 무관하게 허용 (softDelete()와 달리 예외를 던지지 않음)
    public void cascadeSoftDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void adopt() {
        this.isAdopted = true;
    }

    public void cancelAdoption() {
        this.isAdopted = false;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }
}
