package com.likelion.dev_community.domain.reputation.entity;

// F-35 평판 시스템: 활동별 반영 점수
public enum ReputationEvent {

    QUESTION_LIKED(2),
    ANSWER_LIKED(5),
    ANSWER_ADOPTED(15),
    CHAT_ACCEPTED(5),
    CHAT_ADOPTED(20);

    private final int points;

    ReputationEvent(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
