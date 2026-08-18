package com.likelion.dev_community.domain.dashboard.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.dashboard.dto.ActivityTimelineItem;
import com.likelion.dev_community.domain.dashboard.dto.PersonalDashboardSummaryResponse;
import com.likelion.dev_community.domain.dashboard.repository.PersonalDashboardQueryRepository;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalDashboardServiceTest {

    @Mock
    private PersonalDashboardQueryRepository personalDashboardQueryRepository;

    @Mock
    private UserRepository userRepository;

    private PersonalDashboardService personalDashboardService;

    @BeforeEach
    void setUp() {
        personalDashboardService = new PersonalDashboardService(personalDashboardQueryRepository, userRepository);
    }

    @Test
    void 개인_활동_요약을_정상적으로_조회한다() {
        User user = createUser(1L, "member", 22);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(personalDashboardQueryRepository.countQuestionsByAuthor(1L)).thenReturn(3L);
        when(personalDashboardQueryRepository.countAnswersByAuthor(1L)).thenReturn(5L);
        when(personalDashboardQueryRepository.countAdoptedAnswersByAuthor(1L)).thenReturn(2L);
        when(personalDashboardQueryRepository.countUnresolvedQuestionsByAuthor(1L)).thenReturn(1L);

        PersonalDashboardSummaryResponse response = personalDashboardService.getSummary(1L);

        assertThat(response.getQuestionCount()).isEqualTo(3L);
        assertThat(response.getAnswerCount()).isEqualTo(5L);
        assertThat(response.getAdoptedAnswerCount()).isEqualTo(2L);
        assertThat(response.getUnresolvedQuestionCount()).isEqualTo(1L);
        assertThat(response.getReputation()).isEqualTo(22);
    }

    @Test
    void 활동이_없는_사용자는_모든_지표가_0이다() {
        User user = createUser(1L, "newbie", 0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(personalDashboardQueryRepository.countQuestionsByAuthor(1L)).thenReturn(0L);
        when(personalDashboardQueryRepository.countAnswersByAuthor(1L)).thenReturn(0L);
        when(personalDashboardQueryRepository.countAdoptedAnswersByAuthor(1L)).thenReturn(0L);
        when(personalDashboardQueryRepository.countUnresolvedQuestionsByAuthor(1L)).thenReturn(0L);

        PersonalDashboardSummaryResponse response = personalDashboardService.getSummary(1L);

        assertThat(response.getQuestionCount()).isZero();
        assertThat(response.getAnswerCount()).isZero();
        assertThat(response.getAdoptedAnswerCount()).isZero();
        assertThat(response.getUnresolvedQuestionCount()).isZero();
        assertThat(response.getReputation()).isZero();
    }

    @Test
    void 존재하지_않는_사용자면_예외가_발생한다() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalDashboardService.getSummary(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 타임라인은_질문과_답변을_합쳐_최신순으로_정렬한다() {
        User user = createUser(1L, "member", 10);
        Question oldQuestion = createQuestion(10L, user, "오래된 질문", LocalDateTime.of(2026, 8, 1, 10, 0));
        Question newQuestion = createQuestion(11L, user, "최신 질문", LocalDateTime.of(2026, 8, 3, 10, 0));
        Answer middleAnswer = createAnswer(100L, oldQuestion, user, "중간 답변", LocalDateTime.of(2026, 8, 2, 10, 0));

        when(personalDashboardQueryRepository.findRecentQuestionsByAuthor(anyLong(), anyInt()))
                .thenReturn(List.of(newQuestion, oldQuestion));
        when(personalDashboardQueryRepository.findRecentAnswersByAuthor(anyLong(), anyInt()))
                .thenReturn(List.of(middleAnswer));

        List<ActivityTimelineItem> timeline = personalDashboardService.getTimeline(1L);

        assertThat(timeline).hasSize(3);
        assertThat(timeline.get(0).getTitle()).isEqualTo("최신 질문");
        assertThat(timeline.get(1).getTitle()).isEqualTo("중간 답변");
        assertThat(timeline.get(2).getTitle()).isEqualTo("오래된 질문");
    }

    @Test
    void 타임라인_항목은_질문과_답변_타입을_구분한다() {
        User user = createUser(1L, "member", 10);
        Question question = createQuestion(10L, user, "질문 제목", LocalDateTime.of(2026, 8, 2, 10, 0));
        Answer answer = createAnswer(100L, question, user, "답변 내용", LocalDateTime.of(2026, 8, 1, 10, 0));
        answer.adopt();

        when(personalDashboardQueryRepository.findRecentQuestionsByAuthor(anyLong(), anyInt()))
                .thenReturn(List.of(question));
        when(personalDashboardQueryRepository.findRecentAnswersByAuthor(anyLong(), anyInt()))
                .thenReturn(List.of(answer));

        List<ActivityTimelineItem> timeline = personalDashboardService.getTimeline(1L);

        assertThat(timeline.get(0).getType()).isEqualTo("QUESTION");
        assertThat(timeline.get(0).getQuestionId()).isEqualTo(10L);
        assertThat(timeline.get(0).isAdopted()).isFalse();

        // 답변 항목은 답변 자체가 아니라 소속 질문 id를 가리켜야 상세로 이동할 수 있다
        assertThat(timeline.get(1).getType()).isEqualTo("ANSWER");
        assertThat(timeline.get(1).getQuestionId()).isEqualTo(10L);
        assertThat(timeline.get(1).isAdopted()).isTrue();
    }

    @Test
    void 타임라인은_최대_20개까지만_반환한다() {
        User user = createUser(1L, "member", 10);

        List<Question> questions = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            questions.add(createQuestion((long) i, user, "질문" + i, LocalDateTime.of(2026, 8, 1, 0, 0).plusMinutes(i)));
        }
        List<Answer> answers = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            answers.add(createAnswer((long) (100 + i), questions.get(0), user, "답변" + i,
                    LocalDateTime.of(2026, 8, 2, 0, 0).plusMinutes(i)));
        }

        when(personalDashboardQueryRepository.findRecentQuestionsByAuthor(anyLong(), anyInt())).thenReturn(questions);
        when(personalDashboardQueryRepository.findRecentAnswersByAuthor(anyLong(), anyInt())).thenReturn(answers);

        List<ActivityTimelineItem> timeline = personalDashboardService.getTimeline(1L);

        assertThat(timeline).hasSize(20);
        // 답변들이 질문보다 최신이므로 상위 20개는 전부 답변이어야 한다
        assertThat(timeline).allSatisfy(item -> assertThat(item.getType()).isEqualTo("ANSWER"));
    }

    @Test
    void 활동이_없으면_타임라인은_비어있다() {
        when(personalDashboardQueryRepository.findRecentQuestionsByAuthor(anyLong(), anyInt())).thenReturn(List.of());
        when(personalDashboardQueryRepository.findRecentAnswersByAuthor(anyLong(), anyInt())).thenReturn(List.of());

        List<ActivityTimelineItem> timeline = personalDashboardService.getTimeline(1L);

        assertThat(timeline).isEmpty();
    }

    private User createUser(Long id, String nickname, int reputation) {
        User user = User.builder()
                .username(nickname)
                .password("encoded-password")
                .nickname(nickname)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        setId(user, id);
        setField(user, "reputation", reputation);
        return user;
    }

    private Question createQuestion(Long id, User author, String title, LocalDateTime createdAt) {
        Question question = Question.builder()
                .author(author)
                .title(title)
                .content("내용")
                .build();
        setId(question, id);
        setCreatedAt(question, createdAt);
        return question;
    }

    private Answer createAnswer(Long id, Question question, User author, String content, LocalDateTime createdAt) {
        Answer answer = Answer.builder()
                .question(question)
                .author(author)
                .content(content)
                .build();
        setId(answer, id);
        setCreatedAt(answer, createdAt);
        return answer;
    }

    private void setId(Object entity, Long id) {
        setField(entity, "id", id);
    }

    // createdAt은 BaseTimeEntity(상위 클래스)에 있고 Auditing으로 채워지므로 테스트에서는 직접 주입한다
    private void setCreatedAt(Object entity, LocalDateTime createdAt) {
        try {
            Field field = entity.getClass().getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(entity, createdAt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object entity, String fieldName, Object value) {
        try {
            Field field = entity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(entity, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
