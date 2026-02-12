package com.ryduzz.visualkeystrokes.ui.animation;

public final class AnimationHelper {

    private AnimationHelper() {
    }

    public static float easeOutCubic(float t) {
        t = clamp01(t);
        float t1 = t - 1.0f;
        return t1 * t1 * t1 + 1.0f;
    }

    public static float lerp(float from, float to, float progress) {
        return from + (to - from) * clamp01(progress);
    }

    public static int lerpColor(int from, int to, float progress) {
        progress = clamp01(progress);

        int aFrom = (from >> 24) & 0xFF;
        int rFrom = (from >> 16) & 0xFF;
        int gFrom = (from >> 8) & 0xFF;
        int bFrom = from & 0xFF;

        int aTo = (to >> 24) & 0xFF;
        int rTo = (to >> 16) & 0xFF;
        int gTo = (to >> 8) & 0xFF;
        int bTo = to & 0xFF;

        int a = (int) (aFrom + (aTo - aFrom) * progress);
        int r = (int) (rFrom + (rTo - rFrom) * progress);
        int g = (int) (gFrom + (gTo - gFrom) * progress);
        int b = (int) (bFrom + (bTo - bFrom) * progress);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
