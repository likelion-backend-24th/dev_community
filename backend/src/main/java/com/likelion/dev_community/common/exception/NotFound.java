package com.likelion.dev_community.common.exception;

/**
 * 엔티티 조회 실패(404) 시 사용할 대상별 메시지 모음.
 * <p>
 * 같은 대상에 대해 서비스마다 조금씩 다른 문구("질문을 찾을 수 없습니다." / "찾을 수 없는 질문" /
 * "요청한 질문을 찾을 수 없습니다.")를 쓰고 있던 것을 한 곳으로 모은 것이다.
 * 개별 예외 클래스를 만들지 않고 ErrorCode에 정의하는 이 프로젝트의 컨벤션을 그대로 따른다.
 * <p>
 * 사용 예: {@code questionRepository.findById(id).orElseThrow(NotFound.QUESTION::exception)}
 */
public enum NotFound {

    USER("사용자 정보를 찾을 수 없습니다."),
    ACTIVE_USER("서비스 이용 가능 상태가 아닌 사용자입니다."),
    QUESTION("질문을 찾을 수 없습니다."),
    ANSWER("답변을 찾을 수 없습니다."),
    CODE_COMMENT("코멘트를 찾을 수 없습니다."),
    ATTACHMENT("요청한 첨부파일을 찾을 수 없습니다."),
    PAYMENT("결제 정보를 찾을 수 없습니다."),
    REPORT("존재하지 않는 신고입니다."),
    TAG("존재하지 않는 태그");

    private final String message;

    NotFound(String message) {
        this.message = message;
    }

    public CustomException exception() {
        return new CustomException(ErrorCode.NOT_FOUND, message);
    }
}
