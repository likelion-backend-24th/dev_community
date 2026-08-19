package com.likelion.dev_community.domain.question.entity;

import com.likelion.dev_community.common.entity.BaseTimeEntity;
import com.likelion.dev_community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "questions")
@SQLRestriction("deleted_at is null")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private int likeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionStatus status;

    @Column(nullable = false)
    private boolean isPremium;

    @Column(nullable = false)
    private boolean isAnonymous;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionTag> questionTags = new ArrayList<>();

    @Builder
    public Question(User author, String title, String content, boolean isPremium, boolean isAnonymous, QuestionType type) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.viewCount = 0;
        this.likeCount = 0;
        this.status = QuestionStatus.OPEN;
        this.isPremium = isPremium;
        this.isAnonymous = isAnonymous;
        this.type = type != null ? type : QuestionType.GENERAL;
    }

    public void update(String title, String content, boolean isAnonymous, QuestionType type) {
        this.title = title;
        this.content = content;
        this.isAnonymous = isAnonymous;
        this.type = type;
    }

    // F-09
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = QuestionStatus.DELETED;
    }

    public void resolve() {
        this.status = QuestionStatus.RESOLVED;
    }

    public void reopen() {
        this.status = QuestionStatus.OPEN;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    public void createTag(Tag tag) {
        QuestionTag questionTag = QuestionTag.builder()
                .question(this)
                .tag(tag)
                .build();
        this.questionTags.add(questionTag);
    }
}
