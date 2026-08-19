package com.likelion.dev_community.domain.question.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.question.dto.CodeCommentRequest;
import com.likelion.dev_community.domain.question.dto.CodeCommentResponse;
import com.likelion.dev_community.domain.question.entity.CodeComment;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionType;
import com.likelion.dev_community.domain.question.repository.CodeCommentRepository;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.subscription.service.SubscriptionService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeCommentServiceImplTest {

    @Mock
    private CodeCommentRepository codeCommentRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionService subscriptionService;

    private CodeCommentService codeCommentService;

    @BeforeEach
    void setUp() {
        codeCommentService = new CodeCommentService(codeCommentRepository, questionRepository, userRepository, subscriptionService);
    }

    @Test
    void 코드리뷰_질문에_라인_코멘트를_작성한다() {
        User author = createUser(1L, "reviewer");
        Question question = createQuestion(10L, createUser(2L, "asker"), QuestionType.CODE_REVIEW);
        CodeCommentRequest request = new CodeCommentRequest(12, "이 부분 null 체크가 필요해 보여요.");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));

        CodeCommentResponse response = codeCommentService.createComment(1L, false, 10L, request);

        assertThat(response.getLineNumber()).isEqualTo(12);
        assertThat(response.getContent()).isEqualTo("이 부분 null 체크가 필요해 보여요.");
        assertThat(response.getAuthorId()).isEqualTo(1L);
        verify(subscriptionService).requireActiveSubscriber(1L, false);
        verify(codeCommentRepository).save(any());
    }

    @Test
    void 코드리뷰가_아닌_질문에_라인_코멘트_작성시_예외가_발생한다() {
        Question question = createQuestion(10L, createUser(2L, "asker"), QuestionType.GENERAL);
        CodeCommentRequest request = new CodeCommentRequest(1, "내용");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        assertThatThrownBy(() -> codeCommentService.createComment(1L, false, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.CODE_REVIEW_ONLY));
    }

    @Test
    void 비구독자가_라인_코멘트_작성을_시도하면_예외가_발생한다() {
        Question question = createQuestion(10L, createUser(2L, "asker"), QuestionType.CODE_REVIEW);
        CodeCommentRequest request = new CodeCommentRequest(1, "내용");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        doThrow(new CustomException(ErrorCode.FORBIDDEN, "구독자 전용 기능입니다."))
                .when(subscriptionService).requireActiveSubscriber(1L, false);

        assertThatThrownBy(() -> codeCommentService.createComment(1L, false, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN));
    }

    @Test
    void 라인_코멘트_목록을_조회한다() {
        User asker = createUser(2L, "asker");
        Question question = createQuestion(10L, asker, QuestionType.CODE_REVIEW);
        CodeComment comment1 = createComment(1L, question, asker, 5, "5번 라인 코멘트");
        CodeComment comment2 = createComment(2L, question, asker, 10, "10번 라인 코멘트");

        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));
        when(codeCommentRepository.findByQuestionIdOrderByLineNumberAscCreatedAtAsc(10L))
                .thenReturn(List.of(comment1, comment2));

        List<CodeCommentResponse> responses = codeCommentService.readComments(1L, false, 10L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getLineNumber()).isEqualTo(5);
        assertThat(responses.get(1).getLineNumber()).isEqualTo(10);
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

    private CodeComment createComment(Long id, Question question, User author, int lineNumber, String content) {
        CodeComment comment = CodeComment.builder()
                .question(question)
                .author(author)
                .lineNumber(lineNumber)
                .content(content)
                .build();
        setId(comment, id);
        return comment;
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
