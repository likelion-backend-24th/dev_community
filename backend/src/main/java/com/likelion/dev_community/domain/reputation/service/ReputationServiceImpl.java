package com.likelion.dev_community.domain.reputation.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.reputation.entity.ReputationEvent;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReputationServiceImpl implements ReputationService {

    private final UserRepository userRepository;

    @Override
    public void apply(Long userId, ReputationEvent event) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));
        user.increaseReputation(event.getPoints());
    }

    @Override
    public void revert(Long userId, ReputationEvent event) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자 정보를 찾을 수 없습니다."));
        user.decreaseReputation(event.getPoints());
    }
}
