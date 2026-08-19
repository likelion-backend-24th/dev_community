package com.likelion.dev_community.domain.question.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.common.viewcount.ViewCountService;
import com.likelion.dev_community.common.xss.XssSanitizer;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.question.dto.QuestionCreateRequest;
import com.likelion.dev_community.domain.question.dto.QuestionResponse;
import com.likelion.dev_community.domain.question.dto.QuestionSummaryResponse;
import com.likelion.dev_community.domain.question.dto.QuestionUpdateRequest;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionSortType;
import com.likelion.dev_community.domain.question.entity.QuestionType;
import com.likelion.dev_community.domain.question.entity.Tag;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.question.repository.QuestionTagRepository;
import com.likelion.dev_community.domain.question.repository.TagRepository;
import com.likelion.dev_community.domain.subscription.service.SubscriptionService;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionTagRepository questionTagRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ViewCountService viewCountService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private SubscriptionService subscriptionService;

    private QuestionServiceImpl questionService;

    @BeforeEach
    void setUp() {
        questionService = new QuestionServiceImpl(
                questionRepository, questionTagRepository, answerRepository, userRepository,
                new XssSanitizer(), viewCountService, tagRepository, subscriptionService
        );
    }

    @Test
    void 존재하는_태그로_검색하면_해당_태그ID로_조회한다() {
        Tag tag = Tag.builder().name("java").build();
        when(tagRepository.findByName("java")).thenReturn(Optional.of(tag));
        when(questionRepository.searchQuestions(isNull(), any(), isNull(), eq(QuestionSortType.LATEST), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.<Question>of()));

        Page<QuestionSummaryResponse> result = questionService.readQuestions(
                0, 10, null, null, "JAVA", null
        );

        assertThat(result.getContent()).isEmpty();
        verify(questionRepository).searchQuestions(isNull(), any(), isNull(), eq(QuestionSortType.LATEST), any(Pageable.class));
    }

    // ===== readQuestions 정렬 위임 (F-07) =====
    // 주의: 여기서는 sort 문자열이 올바른 QuestionSortType으로 변환되어 리포지토리에
    // "전달"되는지만 검증한다. 실제 정렬 순서(ORDER BY)는 QuestionRepositoryImpl의
    // QueryDSL 로직이 담당하며, 이는 Mockito로 리포지토리를 모킹하는 이 테스트 레벨에서는
    // 검증할 수 없다 (실제 DB/QueryDSL이 개입하는 리포지토리 계층 테스트가 별도로 필요함).

    @Test
    void sort가_latest이면_LATEST로_조회한다() {
        when(questionRepository.searchQuestions(isNull(), isNull(), isNull(), eq(QuestionSortType.LATEST), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        questionService.readQuestions(0, 10, "latest", null, null, null);

        verify(questionRepository).searchQuestions(isNull(), isNull(), isNull(), eq(QuestionSortType.LATEST), any(Pageable.class));
    }

    @Test
    void sort가_like이면_LIKE로_조회한다() {
        when(questionRepository.searchQuestions(isNull(), isNull(), isNull(), eq(QuestionSortType.LIKE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        questionService.readQuestions(0, 10, "like", null, null, null);

        verify(questionRepository).searchQuestions(isNull(), isNull(), isNull(), eq(QuestionSortType.LIKE), any(Pageable.class));
    }

    @Test
    void sort가_unresolved이면_UNRESOLVED로_조회한다() {
        when(questionRepository.searchQuestions(isNull(), isNull(), isNull(), eq(QuestionSortType.UNRESOLVED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        questionService.readQuestions(0, 10, "unresolved", null, null, null);

        verify(questionRepository).searchQuestions(isNull(), isNull(), isNull(), eq(QuestionSortType.UNRESOLVED), any(Pageable.class));
    }

    @Test
    void sort가_없으면_기본값_LATEST로_조회한다() {
        when(questionRepository.searchQuestions(isNull(), isNull(), isNull(), eq(QuestionSortType.LATEST), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        questionService.readQuestions(0, 10, null, null, null, null);

        verify(questionRepository).searchQuestions(isNull(), isNull(), isNull(), eq(QuestionSortType.LATEST), any(Pageable.class));
    }

    // ===== readQuestions page/size (F-07) =====

    @Test
    void page와_size가_Pageable에_그대로_반영되어_전달된다() {
        when(questionRepository.searchQuestions(isNull(), isNull(), isNull(), eq(QuestionSortType.LATEST), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        questionService.readQuestions(2, 20, null, null, null, null);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(questionRepository).searchQuestions(isNull(), isNull(), isNull(), eq(QuestionSortType.LATEST), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void page가_음수이면_INVALID_INPUT() {
        assertThatThrownBy(() -> questionService.readQuestions(-1, 10, null, null, null, null))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 101})
    void size가_1에서_100_범위를_벗어나면_INVALID_INPUT(int invalidSize) {
        assertThatThrownBy(() -> questionService.readQuestions(0, invalidSize, null, null, null, null))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    // ===== updateQuestion / deleteQuestion (F-09) =====

    @Test
    void 본인이_질문을_수정한다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);
        QuestionUpdateRequest request = new QuestionUpdateRequest("새 제목", "새 내용", List.of(), false, null);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        QuestionResponse response = questionService.updateQuestion(10L, 1L, false, request);

        assertThat(response.getTitle()).isEqualTo("새 제목");
        assertThat(response.getContent()).isEqualTo("새 내용");
    }

    @Test
    void 본인이_질문을_삭제한다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(10L)).thenReturn(List.of());

        questionService.deleteQuestion(10L, 1L, false);

        assertThat(question.getDeletedAt()).isNotNull();
    }

    @Test
    void 타인이_질문_수정을_시도하면_FORBIDDEN() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);
        QuestionUpdateRequest request = new QuestionUpdateRequest("제목", "내용", List.of(), false, null);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> questionService.updateQuestion(10L, 999L, false, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void 타인이_질문_삭제를_시도하면_FORBIDDEN() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> questionService.deleteQuestion(10L, 999L, false))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
        assertThat(question.getDeletedAt()).isNull();
    }

    @Test
    void ADMIN은_타인_질문도_삭제할_수_있다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(10L)).thenReturn(List.of());

        questionService.deleteQuestion(10L, 999L, true);

        assertThat(question.getDeletedAt()).isNotNull();
    }

    // ===== attachTags (F-10) =====

    @Test
    void 대소문자와_공백만_다른_태그는_하나로_정규화되어_저장된다() {
        User author = createUser(1L, "author");
        QuestionCreateRequest request = new QuestionCreateRequest("제목", "내용", List.of("Java", " java ", "JAVA"), false, false, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(tagRepository.findByName("java")).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuestionResponse response = questionService.createQuestion(1L, false, request);

        assertThat(response.getTags()).containsExactly("java");
        verify(tagRepository, times(1)).findByName("java");
        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    // ===== 게시글 유형 세분화 (F-42) =====

    @Test
    void 일반_게시판_글은_유형을_요청해도_GENERAL로_고정된다() {
        User author = createUser(1L, "author");
        QuestionCreateRequest request = new QuestionCreateRequest("제목", "내용", List.of(), false, false, "CODE_REVIEW");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        QuestionResponse response = questionService.createQuestion(1L, false, request);

        assertThat(response.getType()).isEqualTo(QuestionType.GENERAL);
    }

    @Test
    void 프리미엄_게시판에서_코드리뷰_유형으로_질문을_작성한다() {
        User author = createUser(1L, "author");
        QuestionCreateRequest request = new QuestionCreateRequest("제목", "내용", List.of(), true, false, "CODE_REVIEW");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        QuestionResponse response = questionService.createQuestion(1L, false, request);

        assertThat(response.getType()).isEqualTo(QuestionType.CODE_REVIEW);
        verify(subscriptionService).requireActiveSubscriber(1L, false);
    }

    @Test
    void 잘못된_유형_문자열이면_INVALID_INPUT() {
        User author = createUser(1L, "author");
        QuestionCreateRequest request = new QuestionCreateRequest("제목", "내용", List.of(), true, false, "INVALID_TYPE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        assertThatThrownBy(() -> questionService.createQuestion(1L, false, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));
    }

    // ===== 익명 질문 (F-41) =====

    @Test
    void 비구독자가_익명으로_질문을_작성하면_FORBIDDEN() {
        User author = createUser(1L, "author");
        QuestionCreateRequest request = new QuestionCreateRequest("제목", "내용", List.of(), false, true, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        doThrow(new CustomException(ErrorCode.FORBIDDEN, "구독자 전용 기능입니다."))
                .when(subscriptionService).requireActiveSubscriber(1L, false);

        assertThatThrownBy(() -> questionService.createQuestion(1L, false, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void 익명으로_작성한_질문은_작성자_닉네임이_익명으로_노출된다() {
        User author = createUser(1L, "author");
        QuestionCreateRequest request = new QuestionCreateRequest("제목", "내용", List.of(), false, true, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        QuestionResponse response = questionService.createQuestion(1L, false, request);

        assertThat(response.getAuthorNickname()).isEqualTo("익명");
        assertThat(response.isAnonymous()).isTrue();
    }

    @Test
    void 비구독자가_질문_수정_시_익명으로_전환하면_FORBIDDEN() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);
        QuestionUpdateRequest request = new QuestionUpdateRequest("제목", "내용", List.of(), true, null);

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        doThrow(new CustomException(ErrorCode.FORBIDDEN, "구독자 전용 기능입니다."))
                .when(subscriptionService).requireActiveSubscriber(1L, false);

        assertThatThrownBy(() -> questionService.updateQuestion(10L, 1L, false, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    // ===== deleteQuestion의 답변 cascade soft delete (F-11) =====
    // 주의: "삭제된 질문의 답변이 목록/검색에서 제외되는지"는 Answer 엔티티의
    // @SQLRestriction("deleted_at is null")이 실제 SQL 생성 시점에 적용되어야 확인 가능한
    // 부분이라, 리포지토리를 모킹하는 이 단위 테스트로는 검증할 수 없다. 여기서는
    // deleteQuestion 호출 시 하위 답변들의 deletedAt이 실제로 채워지는지까지만 검증한다.

    @Test
    void 질문_삭제시_하위_답변도_함께_소프트_삭제된다() {
        User author = createUser(1L, "author");
        Question question = createQuestion(10L, author);
        Answer answer1 = createAnswer(100L, question, createUser(2L, "answerer1"));
        Answer answer2 = createAnswer(101L, question, createUser(3L, "answerer2"));

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionIdOrderByCreatedAtAsc(10L)).thenReturn(List.of(answer1, answer2));

        questionService.deleteQuestion(10L, 1L, false);

        assertThat(answer1.getDeletedAt()).isNotNull();
        assertThat(answer2.getDeletedAt()).isNotNull();
        assertThat(question.getDeletedAt()).isNotNull();
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
                .title("원래 제목")
                .content("원래 내용")
                .build();
        setId(question, id);
        return question;
    }

    private Answer createAnswer(Long id, Question question, User author) {
        Answer answer = Answer.builder()
                .question(question)
                .author(author)
                .content("답변 내용")
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
