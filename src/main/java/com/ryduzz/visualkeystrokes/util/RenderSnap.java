package com.ryduzz.visualkeystrokes.util;

public final class RenderSnap {
    private RenderSnap() {
    }

    public static float snapScale(float scale) {
        return RenderCompat.snapScale(scale);
    }

    public static double snapOffset(int offset, float scale) {
        if (scale == 0.0f) {
            return offset;
        }
        return Math.round(offset * scale) / (double) scale;
    }
}
