package com.likelion.dev_community.domain.answer.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.common.xss.XssSanitizer;
import com.likelion.dev_community.domain.answer.dto.AnswerRequest;
import com.likelion.dev_community.domain.answer.dto.AnswerResponse;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.notification.service.NotificationService;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import com.likelion.dev_community.domain.question.entity.QuestionType;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.reputation.service.ReputationService;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerServiceImplTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReputationService reputationService;

    @Mock
    private NotificationService notificationService;

    private AnswerServiceImpl answerService;

    @BeforeEach
    void setUp() {
        answerService = new AnswerServiceImpl(answerRepository, questionRepository, userRepository, new XssSanitizer(), reputationService, notificationService);
    }

    @Test
    void 정상적으로_답변을_작성한다() {
        User author = createUser(1L, "answerer");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        AnswerRequest request = new AnswerRequest("정말 좋은 질문이네요.");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        AnswerResponse response = answerService.createAnswer(1L, 10L, request);

        assertThat(response.getContent()).isEqualTo("정말 좋은 질문이네요.");
        assertThat(response.getQuestionId()).isEqualTo(10L);
        assertThat(response.getAuthorId()).isEqualTo(1L);
        assertThat(response.getAuthorNickname()).isEqualTo("answerer");
        assertThat(response.isAdopted()).isFalse();
        verify(answerRepository).save(any());
    }

    @Test
    void 존재하지_않는_질문이면_예외가_발생한다() {
        User author = createUser(1L, "answerer");
        AnswerRequest request = new AnswerRequest("내용");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.createAnswer(1L, 999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 정상적으로_답변_목록을_조회한다() {
        User asker = createUser(2L, "asker");
        Question question = createQuestion(10L, asker);
        Answer answer1 = createAnswer(1L, question, createUser(3L, "answerer1"), "첫 번째 답변");
        Answer answer2 = createAnswer(2L, question, createUser(4L, "answerer2"), "두 번째 답변");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(answer1, answer2));

        List<AnswerResponse> responses = answerService.readAnswers(10L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getContent()).isEqualTo("첫 번째 답변");
        assertThat(responses.get(1).getContent()).isEqualTo("두 번째 답변");
    }

    @Test
    void 답변이_없는_질문은_빈_목록을_반환한다() {
        Question question = createQuestion(10L, createUser(2L, "asker"));

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(10L)).thenReturn(List.of());

        List<AnswerResponse> responses = answerService.readAnswers(10L);

        assertThat(responses).isEmpty();
    }

    @Test
    void 답변_목록_조회_시_존재하지_않는_질문이면_예외가_발생한다() {
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.readAnswers(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    // ===== F-42 코드리뷰/커리어상담 =====

    @Test
    void 커리어상담_질문에는_답변을_등록할_수_없다() {
        User author = createUser(1L, "answerer");
        Question question = createQuestion(10L, createUser(2L, "asker"), QuestionType.CAREER_CONSULT);
        AnswerRequest request = new AnswerRequest("답변 시도");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> answerService.createAnswer(1L, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CAREER_CONSULT_ANSWER_NOT_ALLOWED));
    }

    @Test
    void 커리어상담_질문의_답변_목록은_빈_리스트를_반환한다() {
        Question question = createQuestion(10L, createUser(2L, "asker"), QuestionType.CAREER_CONSULT);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        List<AnswerResponse> responses = answerService.readAnswers(10L);

        assertThat(responses).isEmpty();
    }

    @Test
    void 코드리뷰_질문의_답변은_전문가가_상단에_정렬된다() {
        User normalAnswerer = createUser(3L, "answerer1");
        User expertAnswerer = createUser(4L, "answerer2");
        expertAnswerer.grantExpert();

        Question question = createQuestion(10L, createUser(2L, "asker"), QuestionType.CODE_REVIEW);
        Answer normalAnswer = createAnswer(1L, question, normalAnswerer, "일반 답변");
        Answer expertAnswer = createAnswer(2L, question, expertAnswerer, "전문가 답변");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(normalAnswer, expertAnswer));

        List<AnswerResponse> responses = answerService.readAnswers(10L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getContent()).isEqualTo("전문가 답변");
        assertThat(responses.get(1).getContent()).isEqualTo("일반 답변");
    }

    @Test
    void 정상적으로_답변을_수정한다() {
        User author = createUser(1L, "answerer");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(100L, question, author, "원래 내용");
        AnswerRequest request = new AnswerRequest("수정된 내용");

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        AnswerResponse response = answerService.updateAnswer(1L, 100L, false, request);

        assertThat(response.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    void 본인이_아니면_답변_수정시_예외가_발생한다() {
        User author = createUser(1L, "answerer");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(100L, question, author, "원래 내용");
        AnswerRequest request = new AnswerRequest("수정된 내용");

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> answerService.updateAnswer(999L, 100L, false, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void 관리자는_타인_답변도_수정할_수_있다() {
        User author = createUser(1L, "answerer");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(100L, question, author, "원래 내용");
        AnswerRequest request = new AnswerRequest("관리자가 수정한 내용");

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        AnswerResponse response = answerService.updateAnswer(999L, 100L, true, request);

        assertThat(response.getContent()).isEqualTo("관리자가 수정한 내용");
    }

    @Test
    void 존재하지_않는_답변_수정시_예외가_발생한다() {
        AnswerRequest request = new AnswerRequest("수정된 내용");

        when(answerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.updateAnswer(1L, 999L, false, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 정상적으로_답변을_삭제한다() {
        User author = createUser(1L, "answerer");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(100L, question, author, "내용");

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        answerService.deleteAnswer(1L, 100L, false);

        assertThat(answer.getDeletedAt()).isNotNull();
    }

    @Test
    void 본인이_아니면_답변_삭제시_예외가_발생한다() {
        User author = createUser(1L, "answerer");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(100L, question, author, "내용");

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> answerService.deleteAnswer(999L, 100L, false))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
        assertThat(answer.getDeletedAt()).isNull();
    }

    @Test
    void 관리자는_타인_답변도_삭제할_수_있다() {
        User author = createUser(1L, "answerer");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(100L, question, author, "내용");

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        answerService.deleteAnswer(999L, 100L, true);

        assertThat(answer.getDeletedAt()).isNotNull();
    }

    @Test
    void 존재하지_않는_답변_삭제시_예외가_발생한다() {
        when(answerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.deleteAnswer(1L, 999L, false))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 채택된_답변은_삭제할_수_없다() {
        User author = createUser(1L, "answerer");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(100L, question, author, "내용");
        answer.adopt();

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> answerService.deleteAnswer(1L, 100L, false))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.ADOPTED_ANSWER_DELETE_FORBIDDEN));
        assertThat(answer.getDeletedAt()).isNull();
    }

    @Test
    void 정상적으로_답변을_채택한다() {
        User asker = createUser(1L, "asker");
        Question question = createQuestion(10L, asker);
        Answer answer = createAnswer(100L, question, createUser(2L, "answerer"), "내용");

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        AnswerResponse response = answerService.adoptAnswer(1L, 100L);

        assertThat(response.isAdopted()).isTrue();
        assertThat(answer.isAdopted()).isTrue();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.RESOLVED);
    }

    @Test
    void 질문_작성자가_아니면_답변_채택시_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        Question question = createQuestion(10L, asker);
        Answer answer = createAnswer(100L, question, createUser(2L, "answerer"), "내용");

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> answerService.adoptAnswer(999L, 100L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
        assertThat(answer.isAdopted()).isFalse();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.OPEN);
    }

    @Test
    void 이미_채택된_답변을_다시_채택하면_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        Question question = createQuestion(10L, asker);
        Answer answer = createAnswer(100L, question, createUser(2L, "answerer"), "내용");
        answer.adopt();
        question.resolve();

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> answerService.adoptAnswer(1L, 100L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.QUESTION_ALREADY_RESOLVED));
    }

    @Test
    void 이미_RESOLVED인_질문의_다른_답변을_채택하면_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        Question question = createQuestion(10L, asker);
        Answer adoptedAnswer = createAnswer(100L, question, createUser(2L, "answerer1"), "채택된 답변");
        Answer otherAnswer = createAnswer(101L, question, createUser(3L, "answerer2"), "다른 답변");
        adoptedAnswer.adopt();
        question.resolve();

        when(answerRepository.findById(101L)).thenReturn(Optional.of(otherAnswer));

        assertThatThrownBy(() -> answerService.adoptAnswer(1L, 101L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.QUESTION_ALREADY_RESOLVED));
        assertThat(otherAnswer.isAdopted()).isFalse();
    }

    @Test
    void 존재하지_않는_답변_채택시_예외가_발생한다() {
        when(answerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.adoptAnswer(1L, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 정상적으로_답변_채택을_취소한다() {
        User asker = createUser(1L, "asker");
        Question question = createQuestion(10L, asker);
        Answer answer = createAnswer(100L, question, createUser(2L, "answerer"), "내용");
        answer.adopt();
        question.resolve();

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        AnswerResponse response = answerService.cancelAdoption(1L, 100L);

        assertThat(response.isAdopted()).isFalse();
        assertThat(answer.isAdopted()).isFalse();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.OPEN);
    }

    @Test
    void 채택되지_않은_답변을_취소하면_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        Question question = createQuestion(10L, asker);
        Answer answer = createAnswer(100L, question, createUser(2L, "answerer"), "내용");

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> answerService.cancelAdoption(1L, 100L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.ANSWER_NOT_ADOPTED));
    }

    @Test
    void 질문_작성자가_아니면_채택_취소시_예외가_발생한다() {
        User asker = createUser(1L, "asker");
        Question question = createQuestion(10L, asker);
        Answer answer = createAnswer(100L, question, createUser(2L, "answerer"), "내용");
        answer.adopt();
        question.resolve();

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> answerService.cancelAdoption(999L, 100L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
        assertThat(answer.isAdopted()).isTrue();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.RESOLVED);
    }

    @Test
    void 존재하지_않는_답변_채택_취소시_예외가_발생한다() {
        when(answerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.cancelAdoption(1L, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 채택_취소_후_다시_채택할_수_있다() {
        User asker = createUser(1L, "asker");
        Question question = createQuestion(10L, asker);
        Answer answer = createAnswer(100L, question, createUser(2L, "answerer"), "내용");
        answer.adopt();
        question.resolve();

        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        answerService.cancelAdoption(1L, 100L);
        assertThat(answer.isAdopted()).isFalse();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.OPEN);

        AnswerResponse response = answerService.adoptAnswer(1L, 100L);

        assertThat(response.isAdopted()).isTrue();
        assertThat(answer.isAdopted()).isTrue();
        assertThat(question.getStatus()).isEqualTo(QuestionStatus.RESOLVED);
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
        return createQuestion(id, author, QuestionType.GENERAL);
    }

    private Question createQuestion(Long id, User author, QuestionType type) {
        Question question = Question.builder()
                .author(author)
                .title("제목")
                .content("내용")
                .isPremium(type != QuestionType.GENERAL)
                .type(type)
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
