package com.likelion.dev_community.domain.user.dto.authDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SignUpRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void username_앞뒤_공백은_trim되어_저장된다() {
        SignUpRequest request = new SignUpRequest("  leocho  ", "password123", "nickname", "test@test.com");

        assertThat(request.getUsername()).isEqualTo("leocho");
    }

    @Test
    void username이_50자를_초과하면_검증에_실패한다() {
        SignUpRequest request = new SignUpRequest("a".repeat(51), "password123", "nickname", "test@test.com");

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void username이_50자이면_검증을_통과한다() {
        SignUpRequest request = new SignUpRequest("a".repeat(50), "password123", "nickname", "test@test.com");

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void nickname이_30자를_초과하면_검증에_실패한다() {
        SignUpRequest request = new SignUpRequest("username", "password123", "n".repeat(31), "test@test.com");

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void nickname이_30자이면_검증을_통과한다() {
        SignUpRequest request = new SignUpRequest("username", "password123", "n".repeat(30), "test@test.com");

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    // F-25에서 SignUpRequest.nickname에 @NotBlank가 빠져있던 문제가 고쳐졌는지 확인하는 회귀 테스트
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void nickname이_비어있으면_검증에_실패한다(String blankNickname) {
        SignUpRequest request = new SignUpRequest("username", "password123", blankNickname, "test@test.com");

        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }
}
