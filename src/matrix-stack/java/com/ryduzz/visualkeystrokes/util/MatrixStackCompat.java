package com.ryduzz.visualkeystrokes.util;

import net.minecraft.client.util.math.MatrixStack;

public final class MatrixStackCompat {
    private MatrixStackCompat() {
    }

    public static void push(MatrixStack stack) {
        stack.push();
    }

    public static void pop(MatrixStack stack) {
        stack.pop();
    }

    public static void translate(MatrixStack stack, double x, double y) {
        stack.translate(x, y, 0.0d);
    }

    public static void scale(MatrixStack stack, float x, float y) {
        stack.scale(x, y, 1.0f);
    }
}
