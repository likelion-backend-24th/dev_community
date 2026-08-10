package com.likelion.dev_community.domain.question.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionCreateRequestTest {

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
    void title이_100자를_초과하면_검증에_실패한다() {
        QuestionCreateRequest request = new QuestionCreateRequest("t".repeat(101), "내용", List.of());

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
    }

    @Test
    void title이_100자이면_검증을_통과한다() {
        QuestionCreateRequest request = new QuestionCreateRequest("t".repeat(100), "내용", List.of());

        Set<ConstraintViolation<QuestionCreateRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
