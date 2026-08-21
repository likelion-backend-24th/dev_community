package com.likelion.dev_community.domain.answer.dto;

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

class AnswerRequestTest {

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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void content가_비어있으면_검증에_실패한다(String blankContent) {
        AnswerRequest request = new AnswerRequest(blankContent, false);

        Set<ConstraintViolation<AnswerRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("내용을 입력해주세요.");
    }

    @Test
    void content가_존재하면_검증을_통과한다() {
        AnswerRequest request = new AnswerRequest("정상적인 답변 내용입니다.", false);

        Set<ConstraintViolation<AnswerRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
