package com.likelion.dev_community.domain.like.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.like.dto.LikeStatusResponse;
import com.likelion.dev_community.domain.like.entity.LikeHistory;
import com.likelion.dev_community.domain.like.entity.LikeTargetType;
import com.likelion.dev_community.domain.like.repository.LikeHistoryRepository;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionType;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.reputation.entity.ReputationEvent;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private LikeHistoryRepository likeHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private ReputationService reputationService;

    private LikeServiceImpl likeService;

    @BeforeEach
    void setUp() {
        likeService = new LikeServiceImpl(
                likeHistoryRepository, userRepository, questionRepository, answerRepository, reputationService);
    }

    @Test
    void 처음_추천하면_추천이_등록되고_true를_반환한다() {
        User liker = createUser(1L, "liker");
        Question question = createQuestion(10L, createUser(2L, "asker"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(liker));
        when(likeHistoryRepository.findByUserIdAndTargetTypeAndTargetId(1L, LikeTargetType.QUESTION, 10L))
                .thenReturn(Optional.empty());
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        boolean result = likeService.toggleLike(1L, LikeTargetType.QUESTION, 10L);

        assertThat(result).isTrue();
        assertThat(question.getLikeCount()).isEqualTo(1);
        verify(likeHistoryRepository).save(any(LikeHistory.class));
        verify(reputationService).apply(2L, ReputationEvent.QUESTION_LIKED);
    }

    @Test
    void 이미_추천한_상태에서_다시_누르면_추천이_취소되고_false를_반환한다() {
        User liker = createUser(1L, "liker");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        question.increaseLikeCount();
        LikeHistory existing = LikeHistory.builder()
                .user(liker)
                .targetType(LikeTargetType.QUESTION)
                .targetId(10L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(liker));
        when(likeHistoryRepository.findByUserIdAndTargetTypeAndTargetId(1L, LikeTargetType.QUESTION, 10L))
                .thenReturn(Optional.of(existing));
        when(questionRepository.findById(10L)).thenReturn(Optional.of(question));

        boolean result = likeService.toggleLike(1L, LikeTargetType.QUESTION, 10L);

        assertThat(result).isFalse();
        assertThat(question.getLikeCount()).isEqualTo(0);
        verify(likeHistoryRepository).delete(existing);
        verify(reputationService).revert(2L, ReputationEvent.QUESTION_LIKED);
    }

    @Test
    void 존재하지_않는_유저가_추천하면_예외가_발생한다() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.toggleLike(999L, LikeTargetType.QUESTION, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
        verify(likeHistoryRepository, never()).save(any());
    }

    @Test
    void 존재하지_않는_질문을_추천하면_예외가_발생한다() {
        User liker = createUser(1L, "liker");

        when(userRepository.findById(1L)).thenReturn(Optional.of(liker));
        when(likeHistoryRepository.findByUserIdAndTargetTypeAndTargetId(1L, LikeTargetType.QUESTION, 999L))
                .thenReturn(Optional.empty());
        when(questionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.toggleLike(1L, LikeTargetType.QUESTION, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 존재하지_않는_질문의_추천_취소는_조용히_무시된다() {
        User liker = createUser(1L, "liker");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        LikeHistory existing = LikeHistory.builder()
                .user(liker)
                .targetType(LikeTargetType.QUESTION)
                .targetId(10L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(liker));
        when(likeHistoryRepository.findByUserIdAndTargetTypeAndTargetId(1L, LikeTargetType.QUESTION, 10L))
                .thenReturn(Optional.of(existing));
        when(questionRepository.findById(10L)).thenReturn(Optional.empty());

        boolean result = likeService.toggleLike(1L, LikeTargetType.QUESTION, 10L);

        assertThat(result).isFalse();
        verify(likeHistoryRepository).delete(existing);
        verify(reputationService, never()).revert(any(), any());
        assertThat(question.getLikeCount()).isEqualTo(0);
    }

    @Test
    void 답변을_추천하면_답변_작성자에게_평판이_적립된다() {
        User liker = createUser(1L, "liker");
        Question question = createQuestion(10L, createUser(2L, "asker"));
        Answer answer = createAnswer(100L, question, createUser(3L, "answerer"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(liker));
        when(likeHistoryRepository.findByUserIdAndTargetTypeAndTargetId(1L, LikeTargetType.ANSWER, 100L))
                .thenReturn(Optional.empty());
        when(answerRepository.findById(100L)).thenReturn(Optional.of(answer));

        boolean result = likeService.toggleLike(1L, LikeTargetType.ANSWER, 100L);

        assertThat(result).isTrue();
        assertThat(answer.getLikeCount()).isEqualTo(1);
        verify(reputationService).apply(3L, ReputationEvent.ANSWER_LIKED);
    }

    @Test
    void 추천_상태를_조회하면_질문_및_답변_추천여부를_함께_반환한다() {
        when(likeHistoryRepository.existsByUserIdAndTargetTypeAndTargetId(1L, LikeTargetType.QUESTION, 10L))
                .thenReturn(true);
        when(likeHistoryRepository.findLikedTargetIds(1L, LikeTargetType.ANSWER, List.of(100L, 101L)))
                .thenReturn(List.of(100L));

        LikeStatusResponse response = likeService.getLikeStatus(1L, 10L, List.of(100L, 101L));

        assertThat(response.isQuestionLiked()).isTrue();
        assertThat(response.getLikedAnswerIds()).containsExactly(100L);
    }

    @Test
    void 답변_목록이_비어있으면_repository를_호출하지_않고_빈_목록을_반환한다() {
        when(likeHistoryRepository.existsByUserIdAndTargetTypeAndTargetId(1L, LikeTargetType.QUESTION, 10L))
                .thenReturn(false);

        LikeStatusResponse response = likeService.getLikeStatus(1L, 10L, List.of());

        assertThat(response.isQuestionLiked()).isFalse();
        assertThat(response.getLikedAnswerIds()).isEmpty();
        verify(likeHistoryRepository, never()).findLikedTargetIds(any(), any(), any());
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
                .isPremium(false)
                .type(QuestionType.GENERAL)
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
