package com.likelion.dev_community.domain.dashboard.repository;

import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.entity.QAnswer;
import com.likelion.dev_community.domain.question.entity.QQuestion;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

// 개인 활동 대시보드 전용 집계 조회 (읽기 전용, question/answer 도메인 리포지토리는 건드리지 않음)
@Repository
@RequiredArgsConstructor
public class PersonalDashboardQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QQuestion question = QQuestion.question;
    private static final QAnswer answer = QAnswer.answer;

    public long countQuestionsByAuthor(Long userId) {
        return queryFactory.select(question.count())
                .from(question)
                .where(question.author.id.eq(userId))
                .fetchOne();
    }

    public long countAnswersByAuthor(Long userId) {
        return queryFactory.select(answer.count())
                .from(answer)
                .where(answer.author.id.eq(userId))
                .fetchOne();
    }

    public long countAdoptedAnswersByAuthor(Long userId) {
        return queryFactory.select(answer.count())
                .from(answer)
                .where(answer.author.id.eq(userId), answer.isAdopted.isTrue())
                .fetchOne();
    }

    public long countUnresolvedQuestionsByAuthor(Long userId) {
        return queryFactory.select(question.count())
                .from(question)
                .where(question.author.id.eq(userId), question.status.eq(QuestionStatus.OPEN))
                .fetchOne();
    }

    public List<Question> findRecentQuestionsByAuthor(Long userId, int limit) {
        return queryFactory.selectFrom(question)
                .where(question.author.id.eq(userId))
                .orderBy(question.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    public List<Answer> findRecentAnswersByAuthor(Long userId, int limit) {
        return queryFactory.selectFrom(answer)
                .where(answer.author.id.eq(userId))
                .orderBy(answer.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}
