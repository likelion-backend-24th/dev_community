package com.likelion.dev_community.domain.answer.service;

import com.likelion.dev_community.common.AuthorizationValidator;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.common.exception.NotFound;
import com.likelion.dev_community.domain.answer.dto.AnswerRequest;
import com.likelion.dev_community.domain.answer.dto.AnswerResponse;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.notification.service.NotificationService;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import com.likelion.dev_community.domain.question.entity.QuestionType;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.reputation.entity.ReputationEvent;
import com.likelion.dev_community.domain.reputation.service.ReputationService;
import com.likelion.dev_community.domain.subscription.service.SubscriptionService;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ReputationService reputationService;
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;

    // F-12
    @Override
    public AnswerResponse createAnswer(Long userId, boolean isAdmin, Long questionId, AnswerRequest request) {

        User author = userRepository.findById(userId)
                .orElseThrow(NotFound.USER::exception);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(NotFound.QUESTION::exception);

        // 커리어상담 글은 답변 대신 채팅으로 응답
        if (question.getType() == QuestionType.CAREER_CONSULT) {
            throw new CustomException(ErrorCode.CAREER_CONSULT_ANSWER_NOT_ALLOWED);
        }

        if (question.getAuthor().getId().equals(userId)) {
            throw new CustomException(ErrorCode.SELF_ANSWER_NOT_ALLOWED);
        }

        // 익명 답변은 멤버십 게시판 질문, 구독자에 한해 허용
        if (request.isAnonymous()) {
            if (!question.isPremium()) {
                throw new CustomException(ErrorCode.ANONYMOUS_ANSWER_NOT_ALLOWED);
            }
            subscriptionService.requireActiveSubscriber(userId, isAdmin);
        }

        // 본문은 마크다운 원문 그대로 저장(이유는 QuestionServiceImpl의 동일 주석 참고 —
        // 프론트가 rehype-raw 없는 ReactMarkdown만 쓰므로 raw HTML을 렌더링하지 않아 안전함).
        Answer answer = Answer.builder()
                .question(question)
                .author(author)
                .content(request.getContent())
                .isAnonymous(request.isAnonymous())
                .build();

        answerRepository.save(answer);
        notificationService.notifyNewAnswer(question, answer);

        return AnswerResponse.from(answer);
    }

    // 답변 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<AnswerResponse> readAnswers(Long questionId) {

        Question question = questionRepository.findById(questionId).orElseThrow(NotFound.QUESTION::exception);

        // 커리어상담은 답변 목록 대신 채팅 개설 버튼 노출 (프론트)
        if (question.getType() == QuestionType.CAREER_CONSULT) {
            return List.of();
        }

        List<Answer> answers = answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);

        // 코드리뷰 글 전문가 답변을 상단 우선 정렬
        if (question.getType() == QuestionType.CODE_REVIEW) {
            answers = answers.stream()
                    .sorted(Comparator.comparing(answer -> !answer.getAuthor().isExpert()))
                    .toList();
        }

        return answers.stream()
                .map(AnswerResponse::from)
                .toList();
    }

    // 답변 단건 조회
    @Override
    @Transactional(readOnly = true)
    public AnswerResponse getAnswer(Long answerId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(NotFound.ANSWER::exception);

        return AnswerResponse.from(answer);
    }

    // F-13
    @Override
    public AnswerResponse updateAnswer(Long userId, Long answerId, boolean isAdmin, AnswerRequest request) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(NotFound.ANSWER::exception);

        AuthorizationValidator.validateAuthorOrAdmin(answer.getAuthor().getId(), userId, isAdmin, "본인이 작성한 답변만 수정할 수 있습니다.");

        answer.update(request.getContent());

        return AnswerResponse.from(answer);
    }

    // F-13
    @Override
    public void deleteAnswer(Long userId, Long answerId, boolean isAdmin) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(NotFound.ANSWER::exception);

        AuthorizationValidator.validateAuthorOrAdmin(answer.getAuthor().getId(), userId, isAdmin, "본인이 작성한 답변만 삭제할 수 있습니다.");

        answer.softDelete();
    }

    // F-14 채택
    @Override
    public AnswerResponse adoptAnswer(Long userId, Long answerId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(NotFound.ANSWER::exception);

        Question question = answer.getQuestion();

        AuthorizationValidator.validateAuthor(question.getAuthor().getId(), userId, "질문 작성자만 답변을 채택할 수 있습니다.");

        if (question.getStatus() == QuestionStatus.RESOLVED) {
            throw new CustomException(ErrorCode.QUESTION_ALREADY_RESOLVED);
        }

        answer.adopt();
        question.resolve();
        reputationService.apply(answer.getAuthor().getId(), ReputationEvent.ANSWER_ADOPTED);
        notificationService.notifyAnswerAdopted(answer);

        return AnswerResponse.from(answer);
    }

    // F-14-1 채택 취소
    @Override
    public AnswerResponse cancelAdoption(Long userId, Long answerId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(NotFound.ANSWER::exception);

        Question question = answer.getQuestion();

        AuthorizationValidator.validateAuthor(question.getAuthor().getId(), userId, "질문 작성자만 답변 채택을 취소할 수 있습니다.");

        if (!answer.isAdopted()) {
            throw new CustomException(ErrorCode.ANSWER_NOT_ADOPTED);
        }

        answer.cancelAdoption();
        question.reopen();
        reputationService.revert(answer.getAuthor().getId(), ReputationEvent.ANSWER_ADOPTED);

        return AnswerResponse.from(answer);
    }
}
