package com.likelion.dev_community.domain.report.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.report.dto.ReportProcessRequest;
import com.likelion.dev_community.domain.report.dto.ReportRequest;
import com.likelion.dev_community.domain.report.dto.ReportResponse;
import com.likelion.dev_community.domain.report.entity.Report;
import com.likelion.dev_community.domain.report.entity.ReportStatus;
import com.likelion.dev_community.domain.report.entity.ReportTargetType;
import com.likelion.dev_community.domain.report.repository.ReportRepository;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRepository, userRepository, questionRepository, answerRepository);
    }

    @Test
    void 정상적으로_질문을_신고한다() {
        User reporter = createUser(1L, "reporter");
        User author = createUser(2L, "asker");
        Question question = createQuestion(10L, author);
        ReportRequest request = new ReportRequest(ReportTargetType.QUESTION, 10L, "부적절한 내용입니다.");

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(reportRepository.save(any())).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            setId(report, 100L);
            return report;
        });

        ReportResponse response = reportService.report(1L, request);

        assertThat(response.getReporterId()).isEqualTo(1L);
        assertThat(response.getTargetType()).isEqualTo(ReportTargetType.QUESTION);
        assertThat(response.getTargetId()).isEqualTo(10L);
        assertThat(response.getTargetUserId()).isEqualTo(2L);
        assertThat(response.getTargetUserNickname()).isEqualTo("asker");
        assertThat(response.getReason()).isEqualTo("부적절한 내용입니다.");
        assertThat(response.getStatus()).isEqualTo(ReportStatus.PENDING);
        verify(reportRepository).save(any());
    }

    @Test
    void 정상적으로_답변을_신고한다() {
        User reporter = createUser(1L, "reporter");
        User author = createUser(3L, "answerer");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(20L, question, author, "답변 내용");
        ReportRequest request = new ReportRequest(ReportTargetType.ANSWER, 20L, "부적절한 답변입니다.");

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(answerRepository.findById(20L)).thenReturn(Optional.of(answer));
        when(reportRepository.save(any())).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            setId(report, 101L);
            return report;
        });

        ReportResponse response = reportService.report(1L, request);

        assertThat(response.getTargetType()).isEqualTo(ReportTargetType.ANSWER);
        assertThat(response.getTargetId()).isEqualTo(20L);
        assertThat(response.getTargetUserId()).isEqualTo(3L);
        assertThat(response.getTargetUserNickname()).isEqualTo("answerer");
        verify(reportRepository).save(any());
    }

    @Test
    void 본인_질문을_신고하면_예외가_발생한다() {
        User reporter = createUser(1L, "reporter");
        Question question = createQuestion(10L, reporter);
        ReportRequest request = new ReportRequest(ReportTargetType.QUESTION, 10L, "사유");

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> reportService.report(1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.SELF_REPORT_NOT_ALLOWED));
    }

    @Test
    void 본인_답변을_신고하면_예외가_발생한다() {
        User reporter = createUser(1L, "reporter");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(20L, question, reporter, "답변 내용");
        ReportRequest request = new ReportRequest(ReportTargetType.ANSWER, 20L, "사유");

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(answerRepository.findById(20L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> reportService.report(1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.SELF_REPORT_NOT_ALLOWED));
    }

    @Test
    void 존재하지_않는_유저가_신고하면_예외가_발생한다() {
        ReportRequest request = new ReportRequest(ReportTargetType.QUESTION, 10L, "사유");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.report(999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_질문을_신고하면_예외가_발생한다() {
        User reporter = createUser(1L, "reporter");
        ReportRequest request = new ReportRequest(ReportTargetType.QUESTION, 999L, "사유");

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.report(1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_답변을_신고하면_예외가_발생한다() {
        User reporter = createUser(1L, "reporter");
        ReportRequest request = new ReportRequest(ReportTargetType.ANSWER, 999L, "사유");

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(answerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.report(1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 상태값이_없으면_전체_신고_목록을_조회한다() {
        User reporter = createUser(1L, "reporter");
        User targetUser = createUser(2L, "asker");
        Report report = createReport(100L, reporter, ReportTargetType.QUESTION, 10L, 2L, "사유");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(List.of(report), pageable, 1);

        when(reportRepository.findAll(pageable)).thenReturn(reportPage);
        when(userRepository.findAllById(List.of(2L))).thenReturn(List.of(targetUser));

        Page<ReportResponse> result = reportService.getReports(null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTargetUserNickname()).isEqualTo("asker");
    }

    @Test
    void 상태값이_있으면_상태별_신고_목록을_조회한다() {
        User reporter = createUser(1L, "reporter");
        User targetUser = createUser(2L, "asker");
        Report report = createReport(100L, reporter, ReportTargetType.QUESTION, 10L, 2L, "사유");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> reportPage = new PageImpl<>(List.of(report), pageable, 1);

        when(reportRepository.findByStatus(ReportStatus.PENDING, pageable)).thenReturn(reportPage);
        when(userRepository.findAllById(List.of(2L))).thenReturn(List.of(targetUser));

        Page<ReportResponse> result = reportService.getReports(ReportStatus.PENDING, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTargetUserNickname()).isEqualTo("asker");
    }

    @Test
    void REJECTED로_신고를_처리한다() {
        User reporter = createUser(1L, "reporter");
        User targetUser = createUser(2L, "asker");
        Report report = createReport(100L, reporter, ReportTargetType.QUESTION, 10L, 2L, "사유");
        ReportProcessRequest request = new ReportProcessRequest(ReportStatus.REJECTED);

        when(reportRepository.findById(100L)).thenReturn(Optional.of(report));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        ReportResponse response = reportService.processReport(100L, request);

        assertThat(response.getStatus()).isEqualTo(ReportStatus.REJECTED);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.REJECTED);
    }

    @Test
    void RESOLVED로_신고를_처리한다() {
        User reporter = createUser(1L, "reporter");
        User targetUser = createUser(2L, "asker");
        Report report = createReport(100L, reporter, ReportTargetType.QUESTION, 10L, 2L, "사유");
        ReportProcessRequest request = new ReportProcessRequest(ReportStatus.RESOLVED);

        when(reportRepository.findById(100L)).thenReturn(Optional.of(report));
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));

        ReportResponse response = reportService.processReport(100L, request);

        assertThat(response.getStatus()).isEqualTo(ReportStatus.RESOLVED);
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
    }

    @Test
    void 처리_상태가_RESOLVED_REJECTED가_아니면_예외가_발생한다() {
        User reporter = createUser(1L, "reporter");
        Report report = createReport(100L, reporter, ReportTargetType.QUESTION, 10L, 2L, "사유");
        ReportProcessRequest request = new ReportProcessRequest(ReportStatus.PENDING);

        when(reportRepository.findById(100L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> reportService.processReport(100L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
        assertThat(report.getStatus()).isEqualTo(ReportStatus.PENDING);
    }

    @Test
    void 이미_처리된_신고를_다시_처리하면_예외가_발생한다() {
        User reporter = createUser(1L, "reporter");
        Report report = createReport(100L, reporter, ReportTargetType.QUESTION, 10L, 2L, "사유");
        report.resolve();
        ReportProcessRequest request = new ReportProcessRequest(ReportStatus.REJECTED);

        when(reportRepository.findById(100L)).thenReturn(Optional.of(report));

        assertThatThrownBy(() -> reportService.processReport(100L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.ALREADY_PROCESSED_REPORT));
        assertThat(report.getStatus()).isEqualTo(ReportStatus.RESOLVED);
    }

    @Test
    void 존재하지_않는_신고를_처리하면_예외가_발생한다() {
        ReportProcessRequest request = new ReportProcessRequest(ReportStatus.RESOLVED);

        when(reportRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.processReport(999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 유저의_처리된_신고_누적_카운트를_조회한다() {
        when(reportRepository.countByTargetUserIdAndStatus(2L, ReportStatus.RESOLVED)).thenReturn(3L);

        Long count = reportService.countByTargetUserId(2L);

        assertThat(count).isEqualTo(3L);
    }

    private User createUser(Long id, String nickname) {
        User user = User.builder()
                .username(nickname)
                .password("encoded-password")
                .nickname(nickname)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        setId(user, id);
        return user;
    }

    private Question createQuestion(Long id, User author) {
        Question question = Question.builder()
                .author(author)
                .title("제목")
                .content("내용")
                .build();
        setId(question, id);
        return question;
    }

    private Answer createAnswer(Long id, Question question, User author, String content) {
        Answer answer = Answer.builder()
                .question(question)
                .author(author)
                .content(content)
                .build();
        setId(answer, id);
        return answer;
    }

    private Report createReport(Long id, User reporter, ReportTargetType targetType, Long targetId, Long targetUserId, String reason) {
        Report report = Report.builder()
                .reporter(reporter)
                .targetType(targetType)
                .targetId(targetId)
                .targetUserId(targetUserId)
                .reason(reason)
                .build();
        setId(report, id);
        return report;
    }

    private void setId(Object entity, Long id) {
        try {
            Field field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
