package com.likelion.dev_community.domain.subscription.service;

import com.likelion.dev_community.domain.subscription.entity.PlanType;
import com.likelion.dev_community.domain.subscription.entity.Subscription;
import com.likelion.dev_community.domain.subscription.entity.SubscriptionStatus;
import com.likelion.dev_community.domain.subscription.repository.SubscriptionRepository;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void 아직_만료_전인_구독을_갱신하면_남은_기간에_이어서_30일_연장된다() {
        User user = createUser(1L);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(3);
        Subscription subscription = Subscription.create(
                user, PlanType.PREMIUM, LocalDateTime.now().minusDays(27), expiresAt);
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));

        subscriptionService.activateSubscription(user, PlanType.PREMIUM);

        assertThat(subscription.getExpiresAt()).isEqualTo(expiresAt.plusDays(30));
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void 이미_만료된_구독을_갱신하면_오늘부터_30일로_다시_시작한다() {
        User user = createUser(1L);
        Subscription subscription = Subscription.create(
                user, PlanType.PREMIUM, LocalDateTime.now().minusDays(60), LocalDateTime.now().minusDays(30));
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.of(subscription));

        subscriptionService.activateSubscription(user, PlanType.PREMIUM);

        assertThat(subscription.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(29));
        assertThat(subscription.getExpiresAt()).isBefore(LocalDateTime.now().plusDays(31));
    }

    @Test
    void 구독_이력이_없으면_오늘부터_30일짜리_구독을_새로_생성한다() {
        User user = createUser(1L);
        when(subscriptionRepository.findByUserId(1L)).thenReturn(Optional.empty());

        subscriptionService.activateSubscription(user, PlanType.PREMIUM);

        verify(subscriptionRepository).save(any(Subscription.class));
    }

    private User createUser(Long id) {
        User user = User.builder()
                .username("tester")
                .password("encoded-password")
                .nickname("tester")
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
