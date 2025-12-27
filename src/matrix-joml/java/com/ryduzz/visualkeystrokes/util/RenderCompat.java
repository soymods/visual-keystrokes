package com.ryduzz.visualkeystrokes.util;

public final class RenderCompat {
    private RenderCompat() {
    }

    public static float snapScale(float scale) {
        return scale <= 0.0f ? 1.0f : scale;
    }
}
