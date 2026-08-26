package com.likelion.dev_community.common.avatar;

import java.security.SecureRandom;

/**
 * 아바타 원의 배경색 생성 규칙.
 *
 * 색상(hue)만 0~359도 전체에서 무작위로 뽑고 채도/명도는 고정한다. 흰 글자 기준
 * 명암비 4.5:1을 hue 전체 구간(가장 밝게 나오는 노랑~연두 부근 포함)에서 항상
 * 만족하도록 채도 60% · 명도 25%로 고정했다 — hue가 연속값이라 사실상 360가지
 * 이상의 색이 나오면서도 시인성 범위는 유지된다.
 */
public final class AvatarPalette {

    // 익명 작성자 전용 고정 회색. 개인 팔레트에 없는 색이라 실제 이용자를 유추할 수 없다.
    public static final String ANONYMOUS_GRAY = "#57606f";

    private static final float SATURATION = 0.60f;
    private static final float LIGHTNESS = 0.25f;

    private static final SecureRandom RANDOM = new SecureRandom();

    private AvatarPalette() {
    }

    public static String rollRandom() {
        int hue = RANDOM.nextInt(360);
        return toHex(hslToRgb(hue, SATURATION, LIGHTNESS));
    }

    private static int[] hslToRgb(int hueDegrees, float s, float l) {
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float hPrime = hueDegrees / 60f;
        float x = c * (1 - Math.abs(hPrime % 2 - 1));
        float r1, g1, b1;
        if (hPrime < 1) { r1 = c; g1 = x; b1 = 0; }
        else if (hPrime < 2) { r1 = x; g1 = c; b1 = 0; }
        else if (hPrime < 3) { r1 = 0; g1 = c; b1 = x; }
        else if (hPrime < 4) { r1 = 0; g1 = x; b1 = c; }
        else if (hPrime < 5) { r1 = x; g1 = 0; b1 = c; }
        else { r1 = c; g1 = 0; b1 = x; }
        float m = l - c / 2;
        return new int[]{
                Math.round((r1 + m) * 255),
                Math.round((g1 + m) * 255),
                Math.round((b1 + m) * 255)
        };
    }

    private static String toHex(int[] rgb) {
        return String.format("#%02x%02x%02x", rgb[0], rgb[1], rgb[2]);
    }

    // 질문/답변 DTO에서 작성자 아바타 색을 결정하는 공통 규칙.
    // 익명이면 항상 공용 회색, 아니면 작성자의 색(없으면 null → 프론트 기본 파랑).
    public static String resolveAuthorColor(boolean isAnonymous, String authorAvatarColorHex) {
        return isAnonymous ? ANONYMOUS_GRAY : authorAvatarColorHex;
    }
}
