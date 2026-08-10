package com.likelion.dev_community.domain.user.dto.userDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserInfoRequestTest {

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
    void nickname이_30자를_초과하면_검증에_실패한다() {
        UserInfoRequest request = new UserInfoRequest("n".repeat(31));

        Set<ConstraintViolation<UserInfoRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void nickname이_30자이면_검증을_통과한다() {
        UserInfoRequest request = new UserInfoRequest("n".repeat(30));

        Set<ConstraintViolation<UserInfoRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
