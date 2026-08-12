package com.likelion.dev_community.domain.admin.service;

import com.likelion.dev_community.domain.admin.dto.stats.DailyStatItem;
import com.likelion.dev_community.domain.admin.dto.stats.ResolutionRateResponse;
import com.likelion.dev_community.domain.admin.dto.stats.StaleQuestionItem;
import com.likelion.dev_community.domain.admin.dto.stats.StaleQuestionsResponse;
import com.likelion.dev_community.domain.admin.dto.stats.TopQuestionItem;
import com.likelion.dev_community.domain.admin.repository.AdminStatsQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private static final int DAILY_TREND_DAYS = 30;
    private static final int STALE_QUESTION_DAYS = 7;
    private static final int STALE_QUESTION_LIST_LIMIT = 20;
    private static final int TOP_QUESTION_LIMIT = 5;

    private final AdminStatsQueryRepository adminStatsQueryRepository;

    // 최근 30일 일별 가입자/질문 추이
    public List<DailyStatItem> getDailyTrend() {
        return adminStatsQueryRepository.findDailyTrend(DAILY_TREND_DAYS);
    }

    // 질문 해결률
    public ResolutionRateResponse getResolutionRate() {
        long total = adminStatsQueryRepository.countAllQuestions();
        long resolved = adminStatsQueryRepository.countResolvedQuestions();
        return ResolutionRateResponse.of(total, resolved);
    }

    // 7일 이상 답변이 달리지 않은 질문
    public StaleQuestionsResponse getStaleQuestions() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(STALE_QUESTION_DAYS);

        long count = adminStatsQueryRepository.countStaleQuestions(cutoff);
        List<StaleQuestionItem> questions = adminStatsQueryRepository
                .findStaleQuestions(cutoff, STALE_QUESTION_LIST_LIMIT)
                .stream()
                .map(StaleQuestionItem::from)
                .toList();

        return new StaleQuestionsResponse(count, questions);
    }

    // 조회수 상위 Top 5 질문
    public List<TopQuestionItem> getTopQuestions() {
        return adminStatsQueryRepository.findTopQuestionsByViewCount(TOP_QUESTION_LIMIT)
                .stream()
                .map(TopQuestionItem::from)
                .toList();
    }
}
