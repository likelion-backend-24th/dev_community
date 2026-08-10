package com.likelion.dev_community.common;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;

public final class AuthorizationValidator {

    private AuthorizationValidator() {
    }

    // 작성자 본인이 아니면 FORBIDDEN
    public static void validateAuthor(Long actualAuthorId, Long requesterId, String message) {
        if (!actualAuthorId.equals(requesterId)) {
            throw new CustomException(ErrorCode.FORBIDDEN, message);
        }
    }

    // 작성자 본인이거나 ADMIN이 아니면 FORBIDDEN
    public static void validateAuthorOrAdmin(Long actualAuthorId, Long requesterId, boolean isAdmin, String message) {
        if (!actualAuthorId.equals(requesterId) && !isAdmin) {
            throw new CustomException(ErrorCode.FORBIDDEN, message);
        }
    }
}
