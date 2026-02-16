package com.ryduzz.visualkeystrokes.util;

import org.joml.Matrix3x2fStack;

public final class MatrixStackCompat {
    private MatrixStackCompat() {
    }

    public static void push(Matrix3x2fStack stack) {
        stack.pushMatrix();
    }

    public static void pop(Matrix3x2fStack stack) {
        stack.popMatrix();
    }

    public static void translate(Matrix3x2fStack stack, double x, double y) {
        stack.translate((float) x, (float) y);
    }

    public static void scale(Matrix3x2fStack stack, float x, float y) {
        stack.scale(x, y);
    }
}
