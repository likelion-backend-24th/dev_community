package com.likelion.dev_community.common.avatar;

import java.security.SecureRandom;
import java.util.List;

/**
 * 아바타 원의 배경색 팔레트. 프론트엔드({@code frontend/src/utils/avatarColor.js})의
 * MEMBER_COLORS와 값이 정확히 같아야 한다 — 색을 뽑는 건 서버지만, 뽑힌 색을 렌더링하는
 * 스와치 정의(이름·용도)는 양쪽이 같은 목록을 참조한다는 전제로 관리한다.
 *
 * 전부 흰 글자 기준 명암비 4.5:1 이상만 골랐다. Tailwind 600~700 계열 색상 중 대비를
 * 직접 계산해 통과한 것만 남겼다.
 */
public final class AvatarPalette {

    // 익명 작성자 전용 고정 회색. 개인 팔레트에 없는 색이라 실제 이용자를 유추할 수 없다.
    public static final String ANONYMOUS_GRAY = "#57606f";

    public static final List<String> MEMBER_COLORS = List.of(
            "#dc2626", // red
            "#c2410c", // orange (dark)
            "#b45309", // amber (dark)
            "#4d7c0f", // lime (dark)
            "#15803d", // green
            "#047857", // emerald
            "#0f766e", // teal
            "#0e7490", // cyan (dark)
            "#2563eb", // blue
            "#4f46e5", // indigo
            "#6d28d9", // violet
            "#9333ea", // purple
            "#a21caf", // fuchsia (dark)
            "#be185d", // pink (dark)
            "#db2777", // pink
            "#be123c", // rose (dark)
            "#7c2d12", // brown-ish orange
            "#3730a3", // deep indigo
            "#166534", // deep green
            "#155e75", // deep cyan
            "#6b21a8", // deep purple
            "#9d174d", // deep pink
            "#1d4ed8", // strong blue
            "#059669"  // strong emerald
    );

    private static final SecureRandom RANDOM = new SecureRandom();

    private AvatarPalette() {
    }

    public static String rollRandom() {
        return MEMBER_COLORS.get(RANDOM.nextInt(MEMBER_COLORS.size()));
    }

    // 질문/답변 DTO에서 작성자 아바타 색을 결정하는 공통 규칙.
    // 익명이면 항상 공용 회색, 아니면 작성자의 색(없으면 null → 프론트 기본 파랑).
    public static String resolveAuthorColor(boolean isAnonymous, String authorAvatarColorHex) {
        return isAnonymous ? ANONYMOUS_GRAY : authorAvatarColorHex;
    }
}
