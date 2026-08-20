package com.likelion.dev_community.domain.question.entity;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import com.likelion.dev_community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "code_comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CodeComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private int lineNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder
    public CodeComment(Question question, User author, int lineNumber, String content) {
        this.question = question;
        this.author = author;
        this.lineNumber = lineNumber;
        this.content = content;
    }

    public void update(String content){
        this.content = content;
    }
}
