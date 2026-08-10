package com.likelion.dev_community.domain.user.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import com.likelion.dev_community.domain.answer.repository.AnswerRepository;
import com.likelion.dev_community.domain.question.repository.QuestionRepository;
import com.likelion.dev_community.domain.question.repository.QuestionTagRepository;
import com.likelion.dev_community.domain.user.dto.userDto.UserPwRequest;
import com.likelion.dev_community.domain.user.entity.Role;
import com.likelion.dev_community.domain.user.entity.User;
import com.likelion.dev_community.domain.user.entity.UserStatus;
import com.likelion.dev_community.domain.user.repository.RefreshTokenRepository;
import com.likelion.dev_community.domain.user.repository.UserRepository;
import com.likelion.dev_community.security.jwt.CookieProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private CookieProvider cookieProvider;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionTagRepository questionTagRepository;

    @Mock
    private AnswerRepository answerRepository;

    // F-03-1: 현재 비밀번호가 일치하지 않으면 400(PASSWORD_MISMATCH)이어야 한다
    @Test
    void 현재_비밀번호가_일치하지_않으면_400에_해당하는_예외가_발생한다() {
        UserService userService = new UserService(
                userRepository, passwordEncoder, refreshTokenRepository, cookieProvider,
                questionRepository, questionTagRepository, answerRepository
        );

        User user = User.builder()
                .username("tester")
                .password("encoded-old-password")
                .nickname("tester")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-old-password")).thenReturn(false);

        UserPwRequest request = new UserPwRequest("wrong-password", "new-password123");

        assertThatThrownBy(() -> userService.updateUserPassword(request, 1L, new MockHttpServletResponse()))
                .isInstanceOf(CustomException.class)
                .satisfies(e -> {
                    ErrorCode errorCode = ((CustomException) e).getErrorCode();
                    assertThat(errorCode).isEqualTo(ErrorCode.PASSWORD_MISMATCH);
                    assertThat(errorCode.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }
}
