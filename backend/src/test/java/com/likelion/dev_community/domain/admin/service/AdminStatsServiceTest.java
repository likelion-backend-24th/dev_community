package com.likelion.dev_community.domain.admin.service;

import com.likelion.dev_community.domain.admin.dto.stats.DailyStatItem;
import com.likelion.dev_community.domain.admin.dto.stats.ResolutionRateResponse;
import com.likelion.dev_community.domain.admin.dto.stats.StaleQuestionsResponse;
import com.likelion.dev_community.domain.admin.dto.stats.TopQuestionItem;
import com.likelion.dev_community.domain.admin.repository.AdminStatsQueryRepository;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionType;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatsServiceTest {

    @Mock
    private AdminStatsQueryRepository adminStatsQueryRepository;

    private AdminStatsService adminStatsService;

    @BeforeEach
    void setUp() {
        adminStatsService = new AdminStatsService(adminStatsQueryRepository);
    }

    @Test
    void 최근_30일_일별_추이를_그대로_반환한다() {
        List<DailyStatItem> trend = List.of(
                new DailyStatItem(LocalDate.of(2026, 8, 23), 3L, 5L),
                new DailyStatItem(LocalDate.of(2026, 8, 24), 1L, 2L)
        );
        when(adminStatsQueryRepository.findDailyTrend(30)).thenReturn(trend);

        List<DailyStatItem> result = adminStatsService.getDailyTrend();

        assertThat(result).isEqualTo(trend);
        verify(adminStatsQueryRepository).findDailyTrend(30);
    }

    @Test
    void 질문_해결률을_정상적으로_계산한다() {
        when(adminStatsQueryRepository.countAllQuestions()).thenReturn(10L);
        when(adminStatsQueryRepository.countResolvedQuestions()).thenReturn(4L);

        ResolutionRateResponse response = adminStatsService.getResolutionRate();

        assertThat(response.getTotalQuestions()).isEqualTo(10L);
        assertThat(response.getResolvedQuestions()).isEqualTo(4L);
        assertThat(response.getResolutionRate()).isEqualTo(40.0);
    }

    @Test
    void 전체_질문이_없으면_해결률은_0으로_예외없이_계산된다() {
        when(adminStatsQueryRepository.countAllQuestions()).thenReturn(0L);
        when(adminStatsQueryRepository.countResolvedQuestions()).thenReturn(0L);

        ResolutionRateResponse response = adminStatsService.getResolutionRate();

        assertThat(response.getTotalQuestions()).isEqualTo(0L);
        assertThat(response.getResolvedQuestions()).isEqualTo(0L);
        assertThat(response.getResolutionRate()).isEqualTo(0.0);
    }

    @Test
    void 방치된_질문_통계를_7일_기준으로_조회한다() {
        Question stale1 = createQuestion(1L, "방치된 질문1", LocalDateTime.now().minusDays(10));
        Question stale2 = createQuestion(2L, "방치된 질문2", LocalDateTime.now().minusDays(8));

        when(adminStatsQueryRepository.countStaleQuestions(any(LocalDateTime.class))).thenReturn(2L);
        when(adminStatsQueryRepository.findStaleQuestions(any(LocalDateTime.class), eq(20)))
                .thenReturn(List.of(stale1, stale2));

        StaleQuestionsResponse response = adminStatsService.getStaleQuestions();

        assertThat(response.getCount()).isEqualTo(2L);
        assertThat(response.getQuestions()).hasSize(2);
        assertThat(response.getQuestions().get(0).getId()).isEqualTo(1L);
        assertThat(response.getQuestions().get(0).getTitle()).isEqualTo("방치된 질문1");
        assertThat(response.getQuestions().get(1).getId()).isEqualTo(2L);
        assertThat(response.getQuestions().get(1).getTitle()).isEqualTo("방치된 질문2");

        verify(adminStatsQueryRepository).countStaleQuestions(argThat(this::isCloseToSevenDaysAgo));
        verify(adminStatsQueryRepository).findStaleQuestions(argThat(this::isCloseToSevenDaysAgo), eq(20));
    }

    @Test
    void 조회수_상위_질문_5개를_조회한다() {
        Question top1 = createQuestion(1L, "인기 질문1", LocalDateTime.now());
        Question top2 = createQuestion(2L, "인기 질문2", LocalDateTime.now());

        when(adminStatsQueryRepository.findTopQuestionsByViewCount(5)).thenReturn(List.of(top1, top2));

        List<TopQuestionItem> result = adminStatsService.getTopQuestions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("인기 질문1");
        assertThat(result.get(0).getViewCount()).isEqualTo(0);
        assertThat(result.get(0).getLikeCount()).isEqualTo(0);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("인기 질문2");
        verify(adminStatsQueryRepository).findTopQuestionsByViewCount(5);
    }

    private boolean isCloseToSevenDaysAgo(LocalDateTime cutoff) {
        if (cutoff == null) {
            return false;
        }
        long diffSeconds = Math.abs(ChronoUnit.SECONDS.between(cutoff, LocalDateTime.now().minusDays(7)));
        return diffSeconds < 5;
    }

    private User createUser(Long id, String nickname) {
        User user = User.builder()
                .username(nickname)
                .password("encoded-password")
                .nickname(nickname)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        setField(user, "id", id);
        return user;
    }

    private Question createQuestion(Long id, String title, LocalDateTime createdAt) {
        Question question = Question.builder()
                .author(createUser(id + 100, "author" + id))
                .title(title)
                .content("내용")
                .isPremium(false)
                .type(QuestionType.GENERAL)
                .build();
        setField(question, "id", id);
        setField(question, "createdAt", createdAt);
        return question;
    }

    private void setField(Object entity, String fieldName, Object value) {
        Class<?> clazz = entity.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(entity, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("필드를 찾을 수 없습니다: " + fieldName);
    }
}
