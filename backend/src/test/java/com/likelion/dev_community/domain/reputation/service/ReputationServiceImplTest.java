package com.likelion.dev_community.domain.reputation.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.reputation.entity.ReputationEvent;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReputationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private ReputationServiceImpl reputationService;

    @BeforeEach
    void setUp() {
        reputationService = new ReputationServiceImpl(userRepository);
    }

    @Test
    void 평판_적립시_이벤트_점수만큼_증가한다() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        reputationService.apply(1L, ReputationEvent.ANSWER_ADOPTED);

        assertThat(user.getReputation()).isEqualTo(15);
    }

    @Test
    void 여러_이벤트가_누적되면_점수가_합산된다() {
        User user = createUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        reputationService.apply(1L, ReputationEvent.QUESTION_LIKED);
        reputationService.apply(1L, ReputationEvent.ANSWER_LIKED);

        assertThat(user.getReputation()).isEqualTo(7);
    }

    @Test
    void 존재하지_않는_유저에게_적립하면_예외가_발생한다() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reputationService.apply(999L, ReputationEvent.QUESTION_LIKED))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    @Test
    void 평판_회수시_이벤트_점수만큼_감소한다() {
        User user = createUser(1L);
        user.increaseReputation(20);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        reputationService.revert(1L, ReputationEvent.CHAT_ADOPTED);

        assertThat(user.getReputation()).isEqualTo(0);
    }

    @Test
    void 회수시_점수가_0_밑으로_내려가지_않는다() {
        User user = createUser(1L);
        user.increaseReputation(2);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        reputationService.revert(1L, ReputationEvent.ANSWER_ADOPTED);

        assertThat(user.getReputation()).isEqualTo(0);
    }

    @Test
    void 존재하지_않는_유저의_평판을_회수하면_예외가_발생한다() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reputationService.revert(999L, ReputationEvent.QUESTION_LIKED))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> assertThat(((CustomException) e).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND));
    }

    private User createUser(Long id) {
        User user = User.builder()
                .username("user" + id)
                .password("encoded-password")
                .nickname("nick" + id)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        setId(user, id);
        return user;
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
