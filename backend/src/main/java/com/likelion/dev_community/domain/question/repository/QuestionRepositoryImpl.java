package com.likelion.dev_community.domain.question.repository;

import com.likelion.dev_community.domain.question.entity.*;
import com.likelion.dev_community.domain.user.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class QuestionRepositoryImpl implements QuestionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QQuestion question = QQuestion.question;
    private static final QUser author = QUser.user;
    private static final QQuestionTag questionTag = QQuestionTag.questionTag;

    @Override
    public Page<Question> searchQuestions(
            String keyword, Long tagId, QuestionStatus status, QuestionSortType sortType, Pageable pageable
    ) {
        return search(keyword, tagId, status, sortType, pageable, false);
    }

    @Override
    public Page<Question> searchPremiumQuestions(
            String keyword, Long tagId, QuestionStatus status, QuestionSortType sortType, Pageable pageable
    ) {
        return search(keyword, tagId, status, sortType, pageable, true);
    }

    private Page<Question> search(
            String keyword, Long tagId, QuestionStatus status, QuestionSortType sortType, Pageable pageable, boolean isPremium
    ) {
        BooleanBuilder condition = new BooleanBuilder();
        condition.and(question.isPremium.eq(isPremium));

        if (keyword != null && !keyword.isBlank()) {
            BooleanBuilder keywordCondition = new BooleanBuilder();
            keywordCondition.or(question.title.containsIgnoreCase(keyword));
            keywordCondition.or(question.content.containsIgnoreCase(keyword));
            keywordCondition.or(author.nickname.containsIgnoreCase(keyword));
            condition.and(keywordCondition);
        }

        // F-18
        if (tagId != null) {
            condition.and(questionTag.tag.id.eq(tagId));
        }

        // F-19
        if (status != null) {
            condition.and(question.status.eq(status));
        }

        List<Question> content = queryFactory
                .selectFrom(question)
                .join(question.author, author).fetchJoin()
                .leftJoin(question.questionTags, questionTag)
                .where(condition)
                .orderBy(resolveOrder(sortType))
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(question.countDistinct())
                .from(question)
                .join(question.author, author)
                .leftJoin(question.questionTags, questionTag)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private OrderSpecifier<?>[] resolveOrder(QuestionSortType sortType) {
        return switch (sortType) {
            case LIKE -> new OrderSpecifier<?>[]{ question.likeCount.desc() };
            case UNRESOLVED -> new OrderSpecifier<?>[]{
                    new CaseBuilder()
                            .when(question.status.eq(QuestionStatus.OPEN)).then(0)
                            .otherwise(1).asc(),
                    question.createdAt.desc()
            };
            case LATEST -> new OrderSpecifier<?>[]{ question.createdAt.desc() };
        };
    }
}