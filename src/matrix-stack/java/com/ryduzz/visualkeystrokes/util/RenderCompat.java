package com.ryduzz.visualkeystrokes.util;

public final class RenderCompat {
    private RenderCompat() {
    }

    public static float snapScale(float scale) {
        if (scale <= 0.0f) {
            return 1.0f;
        }
        float snapped = Math.round(scale * 2.0f) / 2.0f;
        return Math.max(0.5f, snapped);
    }
}
