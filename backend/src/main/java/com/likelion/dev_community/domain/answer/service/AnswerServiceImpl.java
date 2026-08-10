package com.likelion.dev_community.domain.answer.service;

import com.likelion.dev_community.common.AuthorizationValidator;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.common.xss.XssSanitizer;
import com.likelion.dev_community.domain.answer.dto.AnswerRequest;
import com.likelion.dev_community.domain.answer.dto.AnswerResponse;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.entity.QuestionStatus;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final XssSanitizer xssSanitizer;

    // F-12
    @Override
    public AnswerResponse createAnswer(Long userId, Long questionId, AnswerRequest request) {

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "질문을 찾을 수 없습니다."));

        String content = xssSanitizer.sanitize(request.getContent());

        Answer answer = Answer.builder()
                .question(question)
                .author(author)
                .content(content)
                .build();

        answerRepository.save(answer);

        return AnswerResponse.from(answer);
    }

    // 답변 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<AnswerResponse> readAnswers(Long questionId) {

        if (!questionRepository.existsById(questionId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "질문을 찾을 수 없습니다.");
        }

        return answerRepository.findByQuestionIdOrderByCreatedAtAsc(questionId).stream()
                .map(AnswerResponse::from)
                .toList();
    }

    // F-13
    @Override
    public AnswerResponse updateAnswer(Long userId, Long answerId, AnswerRequest request) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "답변을 찾을 수 없습니다."));

        AuthorizationValidator.validateAuthor(answer.getAuthor().getId(), userId, "본인이 작성한 답변만 수정할 수 있습니다.");

        String content = xssSanitizer.sanitize(request.getContent());
        answer.update(content);

        return AnswerResponse.from(answer);
    }

    // F-13
    @Override
    public void deleteAnswer(Long userId, Long answerId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "답변을 찾을 수 없습니다."));

        AuthorizationValidator.validateAuthor(answer.getAuthor().getId(), userId, "본인이 작성한 답변만 삭제할 수 있습니다.");

        answer.softDelete();
    }

    // F-14 채택
    @Override
    public AnswerResponse adoptAnswer(Long userId, Long answerId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "답변을 찾을 수 없습니다."));

        Question question = answer.getQuestion();

        AuthorizationValidator.validateAuthor(question.getAuthor().getId(), userId, "질문 작성자만 답변을 채택할 수 있습니다.");

        if (question.getStatus() == QuestionStatus.RESOLVED) {
            throw new CustomException(ErrorCode.QUESTION_ALREADY_RESOLVED);
        }

        answer.adopt();
        question.resolve();

        return AnswerResponse.from(answer);
    }

    // F-14-1 채택 취소
    @Override
    public AnswerResponse cancelAdoption(Long userId, Long answerId) {

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "답변을 찾을 수 없습니다."));

        Question question = answer.getQuestion();

        AuthorizationValidator.validateAuthor(question.getAuthor().getId(), userId, "질문 작성자만 답변 채택을 취소할 수 있습니다.");

        if (!answer.isAdopted()) {
            throw new CustomException(ErrorCode.ANSWER_NOT_ADOPTED);
        }

        answer.cancelAdoption();
        question.reopen();

        return AnswerResponse.from(answer);
    }
}
