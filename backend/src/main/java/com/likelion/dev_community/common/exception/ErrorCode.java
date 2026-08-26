package com.likelion.dev_community.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "입력값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "이미 존재하는 리소스입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "서버 내부 오류가 발생했습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,"INVALID_CREDENTIALS","아이디 또는 비밀번호가 올바르지 않습니다."),
    SUSPENDED_ACCOUNT(HttpStatus.UNAUTHORIZED,"SUSPENDED_ACCOUNT","정지된 계정입니다."),
    WITHDRAWN_ACCOUNT(HttpStatus.UNAUTHORIZED,"WITHDRAWN_ACCOUNT","탈퇴한 계정입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED,"INVALID_REFRESH_TOKEN","유효하지 않은 토큰"),
    EXPIRED_REFRESH_TOKEN(HttpStatus.FORBIDDEN,"EXPIRED_REFRESH_TOKEN","만료된 토큰"),
    ACCOUNT_LOCKED(HttpStatus.TOO_MANY_REQUESTS,"ACCOUNT_LOCKED","로그인 시도 횟수를 초과했습니다. 15분 후 다시 시도해주세요."),
    PASSWORD_RESET_TOKEN_INVALID(HttpStatus.BAD_REQUEST,"PASSWORD_RESET_TOKEN_INVALID","유효하지 않거나 만료된 링크입니다."),
    PASSWORD_RESET_EMAIL_FAILED(HttpStatus.BAD_GATEWAY,"PASSWORD_RESET_EMAIL_FAILED","이메일 발송에 실패했습니다."),
    QUESTION_ALREADY_RESOLVED(HttpStatus.CONFLICT, "QUESTION_ALREADY_RESOLVED", "이미 채택된 답변이 존재하는 질문입니다."),
    ANSWER_NOT_ADOPTED(HttpStatus.CONFLICT, "ANSWER_NOT_ADOPTED", "채택되지 않은 답변입니다."),
    ADOPTED_ANSWER_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "ADOPTED_ANSWER_DELETE_FORBIDDEN", "접근 권한이 없습니다. 채택된 답변은 삭제할 수 없습니다. 먼저 채택을 취소해주세요."),

    SELF_REPORT_NOT_ALLOWED(HttpStatus.FORBIDDEN,"SELF_REPORT_NOT_ALLOWED", "자신의 게시물, 답변은 신고할 수 없습니다."),
    SELF_ANSWER_NOT_ALLOWED(HttpStatus.FORBIDDEN, "SELF_ANSWER_NOT_ALLOWED", "본인의 글에는 답변을 등록할 수 없습니다."),
    ALREADY_PROCESSED_REPORT(HttpStatus.CONFLICT, "ALREADY_PROCESSED_REPORT", "이미 처리된 신고입니다."),

    ALREADY_SUSPENDED_USER(HttpStatus.CONFLICT, "ALREADY_SUSPENDED_USER", "이미 정지 처리된 사용자입니다."),
    NOT_SUSPENDED_USER(HttpStatus.CONFLICT, "NOT_SUSPENDED_USER", "정지 상태가 아닌 사용자입니다"),
    ALREADY_EXPERT(HttpStatus.CONFLICT, "ALREADY_EXPERT", "이미 전문가로 지정된 사용자입니다."),
    ALREADY_REQUESTED_EXPERT(HttpStatus.CONFLICT, "ALREADY_REQUESTED_EXPERT", "이미 전문가 등급을 신청했습니다."),
    NOT_REQUESTED_EXPERT(HttpStatus.CONFLICT, "NOT_REQUESTED_EXPERT", "전문가 등급을 신청하지 않은 사용자입니다."),

    OAUTH_LOGIN_FAILED(HttpStatus.BAD_GATEWAY, "OAUTH_LOGIN_FAILED", "소셜 로그인 처리 중 오류가 발생했습니다."),
    OAUTH_SIGNUP_EXPIRED(HttpStatus.BAD_REQUEST, "OAUTH_SIGNUP_EXPIRED", "인증 정보가 만료되었습니다. 다시 로그인해주세요."),
    PAYMENT_VERIFICATION_FAILED(HttpStatus.CONFLICT, "PAYMENT_VERIFICATION_FAILED", "PortOne 결제 정보 검증에 실패했습니다."),
    PAYMENT_NOT_CANCELLABLE(HttpStatus.CONFLICT, "PAYMENT_NOT_CANCELLABLE", "취소할 수 없는 결제입니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_GATEWAY, "PAYMENT_CANCEL_FAILED", "PortOne 결제 취소 요청에 실패했습니다."),

    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE", "파일 용량이 너무 큽니다. (최대 2MB)"),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "UNSUPPORTED_FILE_TYPE", "지원하지 않는 파일 형식입니다."),
    ATTACHMENT_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "ATTACHMENT_LIMIT_EXCEEDED", "첨부파일은 최대 5개까지 등록할 수 있습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다."),

    CAREER_CONSULT_ANSWER_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CAREER_CONSULT_ANSWER_NOT_ALLOWED", "커리어상담 게시글에는 답변을 등록할 수 없습니다."),
    CODE_REVIEW_ONLY(HttpStatus.BAD_REQUEST, "CODE_REVIEW_ONLY", "코드리뷰 게시글에만 라인 코멘트를 남길 수 있습니다."),
    ANONYMOUS_ANSWER_NOT_ALLOWED(HttpStatus.FORBIDDEN, "ANONYMOUS_ANSWER_NOT_ALLOWED", "멤버십 게시판의 질문에만 익명으로 답변할 수 있습니다."),

    CHAT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CHAT_NOT_ALLOWED", "커리어상담 게시글에서만 1:1 채팅을 사용할 수 있습니다."),
    CHAT_SELF_NOT_ALLOWED(HttpStatus.FORBIDDEN, "CHAT_SELF_NOT_ALLOWED", "본인의 글에는 채팅을 개설할 수 없습니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_ROOM_NOT_FOUND", "채팅방을 찾을 수 없습니다."),
    CHAT_FORBIDDEN(HttpStatus.FORBIDDEN, "CHAT_FORBIDDEN", "해당 채팅방에 접근할 권한이 없습니다."),
    CHAT_FIRST_MESSAGE_LIMIT(HttpStatus.CONFLICT, "CHAT_FIRST_MESSAGE_LIMIT", "채팅 수락 전에는 최초 1회만 메시지를 보낼 수 있습니다."),
    CHAT_NOT_ACTIVE(HttpStatus.CONFLICT, "CHAT_NOT_ACTIVE", "진행 중인 채팅이 아닙니다."),
    CHAT_ALREADY_ACCEPTED(HttpStatus.CONFLICT, "CHAT_ALREADY_ACCEPTED", "이미 수락된 채팅입니다."),
    CHAT_NOT_ACCEPTED(HttpStatus.CONFLICT, "CHAT_NOT_ACCEPTED", "아직 수락되지 않은 채팅입니다."),
    QUESTION_TYPE_LOCKED(HttpStatus.CONFLICT, "QUESTION_TYPE_LOCKED", "채팅이 시작된 질문은 글 유형을 변경할 수 없습니다."),
    QUESTION_CONTENT_LOCKED(HttpStatus.CONFLICT, "QUESTION_CONTENT_LOCKED", "코드 코멘트가 달린 질문은 본문을 수정할 수 없습니다."),

    AI_SUMMARY_TOO_SHORT(HttpStatus.BAD_REQUEST, "AI_SUMMARY_TOO_SHORT", "요약하기엔 너무 짧은 질문입니다."),
    AI_SUMMARY_FAILED(HttpStatus.BAD_GATEWAY, "AI_SUMMARY_FAILED", "AI 요약 생성에 실패했습니다."),

    AI_TAG_SUGGESTION_TOO_SHORT(HttpStatus.BAD_REQUEST, "AI_TAG_SUGGESTION_TOO_SHORT", "태그를 추천하기엔 본문이 너무 짧습니다."),
    AI_TAG_SUGGESTION_FAILED(HttpStatus.BAD_GATEWAY, "AI_TAG_SUGGESTION_FAILED", "AI 태그 추천에 실패했습니다."),

    AVATAR_COLOR_ALREADY_ROLLED(HttpStatus.CONFLICT, "AVATAR_COLOR_ALREADY_ROLLED", "이번 결제 주기에는 이미 아바타 색상을 변경했어요.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
