package com.likelion.dev_community.common;

import com.likelion.dev_community.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 요청_본문_파싱_실패시_500이_아닌_400을_반환한다() {
        HttpInputMessage emptyMessage = new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }
        };
        HttpMessageNotReadableException e =
                new HttpMessageNotReadableException("Cannot map `null` into type `boolean`", emptyMessage);

        ResponseEntity<ApiResponse<Void>> response = handler.handleHttpMessageNotReadableException(e);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getCode()).isEqualTo(ErrorCode.INVALID_INPUT.getCode());
    }
}
