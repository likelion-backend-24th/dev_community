package com.likelion.dev_community.domain.like.service;

import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.domain.answer.entity.Answer;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.like.dto.LikeStatusResponse;
import com.likelion.dev_community.domain.like.entity.LikeHistory;
import com.likelion.dev_community.domain.like.entity.LikeTargetType;
import com.likelion.dev_community.domain.like.repository.LikeHistoryRepository;
import com.likelion.dev_community.domain.question.entity.Question;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.reputation.entity.ReputationEvent;
import com.likelion.dev_community.domain.reputation.service.ReputationService;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeServiceImpl implements LikeService {

    private final LikeHistoryRepository likeHistoryRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final ReputationService reputationService;

    @Override
    public boolean toggleLike(Long userId, LikeTargetType targetType, Long targetId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        return likeHistoryRepository
                .findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .map(existing -> {
                    likeHistoryRepository.delete(existing);
                    decreaseCount(targetType, targetId);
                    return false;
                })
                .orElseGet(() -> {
                    // 자기 자신의 글/답변은 애초에 좋아요를 만들 수 없으므로, 취소(위 map)가 아닌
                    // 신규 등록 시점에서만 검사한다.
                    if (resolveAuthorId(targetType, targetId).equals(userId)) {
                        throw new CustomException(ErrorCode.SELF_LIKE_NOT_ALLOWED);
                    }
                    // UNIQUE(user_id, target_type, target_id) 제약이 최종 방어선이므로
                    // 동시 요청으로 두 번 눌려도 DB가 두 번째 insert를 막아준다.
                    likeHistoryRepository.save(
                            LikeHistory.builder()
                                    .user(user)
                                    .targetType(targetType)
                                    .targetId(targetId)
                                    .build()
                    );
                    increaseCount(targetType, targetId);
                    return true;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public LikeStatusResponse getLikeStatus(Long userId, Long questionId, List<Long> answerIds) {
        boolean questionLiked = likeHistoryRepository
                .existsByUserIdAndTargetTypeAndTargetId(userId, LikeTargetType.QUESTION, questionId);

        List<Long> likedAnswerIds = answerIds.isEmpty()
                ? List.of()
                : likeHistoryRepository.findLikedTargetIds(userId, LikeTargetType.ANSWER, answerIds);

        return new LikeStatusResponse(questionLiked, likedAnswerIds);
    }

    private Long resolveAuthorId(LikeTargetType targetType, Long targetId) {
        if (targetType == LikeTargetType.QUESTION) {
            return questionRepository.findById(targetId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND))
                    .getAuthor().getId();
        }
        return answerRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND))
                .getAuthor().getId();
    }

    private void increaseCount(LikeTargetType targetType, Long targetId) {
        if (targetType == LikeTargetType.QUESTION) {
            Question question = questionRepository.findById(targetId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
            question.increaseLikeCount();
            reputationService.apply(question.getAuthor().getId(), ReputationEvent.QUESTION_LIKED);
        } else {
            Answer answer = answerRepository.findById(targetId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
            answer.increaseLikeCount();
            reputationService.apply(answer.getAuthor().getId(), ReputationEvent.ANSWER_LIKED);
        }
    }

    private void decreaseCount(LikeTargetType targetType, Long targetId) {
        if (targetType == LikeTargetType.QUESTION) {
            questionRepository.findById(targetId).ifPresent(question -> {
                question.decreaseLikeCount();
                reputationService.revert(question.getAuthor().getId(), ReputationEvent.QUESTION_LIKED);
            });
        } else {
            answerRepository.findById(targetId).ifPresent(answer -> {
                answer.decreaseLikeCount();
                reputationService.revert(answer.getAuthor().getId(), ReputationEvent.ANSWER_LIKED);
            });
        }
    }
}
