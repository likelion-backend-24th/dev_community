package com.likelion.dev_community.domain.admin.repository;

import com.likelion.dev_community.domain.admin.dto.stats.DailyStatItem;
import com.likelion.dev_community.domain.answer.entity.QAnswer;
import com.likelion.dev_community.domain.question.entity.QQuestion;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import com.likelion.dev_community.domain.user.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 관리자 통계 대시보드 전용 집계 조회 (읽기 전용, 다른 도메인 리포지토리는 건드리지 않음)
@Repository
@RequiredArgsConstructor
public class AdminStatsQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QQuestion question = QQuestion.question;
    private static final QAnswer answer = QAnswer.answer;
    private static final QUser user = QUser.user;

    // 최근 N일간 일별 가입자 수 / 질문 수
    public List<DailyStatItem> findDailyTrend(int days) {
        LocalDate today = LocalDate.now();
        LocalDate fromDate = today.minusDays(days - 1L);
        LocalDateTime from = fromDate.atStartOfDay();

        DateTemplate<Date> signupDate = Expressions.dateTemplate(Date.class, "DATE({0})", user.createdAt);
        Map<LocalDate, Long> signupCounts = new HashMap<>();
        for (Tuple row : queryFactory
                .select(signupDate, user.count())
                .from(user)
                .where(user.createdAt.goe(from))
                .groupBy(signupDate)
                .fetch()) {
            signupCounts.put(row.get(signupDate).toLocalDate(), row.get(user.count()));
        }

        DateTemplate<Date> questionDate = Expressions.dateTemplate(Date.class, "DATE({0})", question.createdAt);
        Map<LocalDate, Long> questionCounts = new HashMap<>();
        for (Tuple row : queryFactory
                .select(questionDate, question.count())
                .from(question)
                .where(question.createdAt.goe(from))
                .groupBy(questionDate)
                .fetch()) {
            questionCounts.put(row.get(questionDate).toLocalDate(), row.get(question.count()));
        }

        return fromDate.datesUntil(today.plusDays(1))
                .map(date -> new DailyStatItem(
                        date,
                        signupCounts.getOrDefault(date, 0L),
                        questionCounts.getOrDefault(date, 0L)
                ))
                .toList();
    }

    // 전체 질문 수 / 해결된 질문 수
    public long countAllQuestions() {
        return queryFactory.select(question.count()).from(question).fetchOne();
    }

    public long countResolvedQuestions() {
        return queryFactory.select(question.count())
                .from(question)
                .where(question.status.eq(QuestionStatus.RESOLVED))
                .fetchOne();
    }

    // 답변이 하나도 없고, cutoff 이전에 작성된(N일 이상 방치된) 질문
    public long countStaleQuestions(LocalDateTime cutoff) {
        return queryFactory.select(question.count())
                .from(question)
                .where(
                        question.status.eq(QuestionStatus.OPEN),
                        question.createdAt.before(cutoff),
                        hasNoAnswer()
                )
                .fetchOne();
    }

    public List<Question> findStaleQuestions(LocalDateTime cutoff, int limit) {
        return queryFactory.selectFrom(question)
                .where(
                        question.status.eq(QuestionStatus.OPEN),
                        question.createdAt.before(cutoff),
                        hasNoAnswer()
                )
                .orderBy(question.createdAt.asc())
                .limit(limit)
                .fetch();
    }

    private com.querydsl.core.types.dsl.BooleanExpression hasNoAnswer() {
        return JPAExpressions.selectOne()
                .from(answer)
                .where(answer.question.eq(question))
                .notExists();
    }

    // 조회수 상위 Top N 질문
    public List<Question> findTopQuestionsByViewCount(int limit) {
        return queryFactory.selectFrom(question)
                .orderBy(question.viewCount.desc())
                .limit(limit)
                .fetch();
    }
}
