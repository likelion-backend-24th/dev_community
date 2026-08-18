package com.likelion.dev_community.domain.dashboard.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.dashboard.dto.ActivityTimelineItem;
import com.likelion.dev_community.domain.dashboard.dto.PersonalDashboardSummaryResponse;
import com.likelion.dev_community.domain.dashboard.repository.PersonalDashboardQueryRepository;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalDashboardService {

    private static final int TIMELINE_LIMIT = 20;

    private final PersonalDashboardQueryRepository personalDashboardQueryRepository;
    private final UserRepository userRepository;

    public PersonalDashboardSummaryResponse getSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));

        long questionCount = personalDashboardQueryRepository.countQuestionsByAuthor(userId);
        long answerCount = personalDashboardQueryRepository.countAnswersByAuthor(userId);
        long adoptedAnswerCount = personalDashboardQueryRepository.countAdoptedAnswersByAuthor(userId);
        long unresolvedQuestionCount = personalDashboardQueryRepository.countUnresolvedQuestionsByAuthor(userId);

        return new PersonalDashboardSummaryResponse(
                questionCount,
                answerCount,
                adoptedAnswerCount,
                unresolvedQuestionCount,
                user.getReputation()
        );
    }

    public List<ActivityTimelineItem> getTimeline(Long userId) {
        Stream<ActivityTimelineItem> questionItems = personalDashboardQueryRepository
                .findRecentQuestionsByAuthor(userId, TIMELINE_LIMIT)
                .stream()
                .map(ActivityTimelineItem::from);

        Stream<ActivityTimelineItem> answerItems = personalDashboardQueryRepository
                .findRecentAnswersByAuthor(userId, TIMELINE_LIMIT)
                .stream()
                .map(ActivityTimelineItem::from);

        return Stream.concat(questionItems, answerItems)
                .sorted(Comparator.comparing(ActivityTimelineItem::getCreatedAt).reversed())
                .limit(TIMELINE_LIMIT)
                .toList();
    }
}
