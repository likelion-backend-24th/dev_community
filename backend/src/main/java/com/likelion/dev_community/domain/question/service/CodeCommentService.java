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
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CodeCommentService{

    private final CodeCommentRepository codeCommentRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    // 코드 코멘트 작성
    public CodeCommentResponse createComment(Long userId, boolean isAdmin, Long questionId, CodeCommentRequest request) {

        Question question = questionRepository.findById(questionId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "질문을 찾을 수 없습니다."));

        validateCodeReviewQuestion(question, userId, isAdmin);

        User author = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));

        CodeComment comment = CodeComment.builder()
                .question(question)
                .author(author)
                .lineNumber(request.getLineNumber())
                .content(request.getContent())
                .build();

        codeCommentRepository.save(comment);

        return CodeCommentResponse.from(comment);
    }

    // 코드 코멘트 조회
    @Transactional(readOnly = true)
    public List<CodeCommentResponse> readComments(Long userId, boolean isAdmin, Long questionId) {

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "질문을 찾을 수 없습니다."));

        validateCodeReviewQuestion(question, userId, isAdmin);

        return codeCommentRepository.findByQuestionIdOrderByLineNumberAscCreatedAtAsc(questionId).stream()
                .map(CodeCommentResponse::from)
                .toList();
    }

    private void validateCodeReviewQuestion(Question question, Long userId, boolean isAdmin) {
        if (question.getType() != QuestionType.CODE_REVIEW) {
            throw new CustomException(ErrorCode.CODE_REVIEW_ONLY);
        }

        if (question.isPremium()) {
            subscriptionService.requireActiveSubscriber(userId, isAdmin);
        }
    }
}
