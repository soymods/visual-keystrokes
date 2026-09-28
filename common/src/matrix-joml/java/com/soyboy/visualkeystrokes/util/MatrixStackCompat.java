package com.soyboy.visualkeystrokes.util;

import org.joml.Matrix3x2fStack;
import org.joml.Vector2f;

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

    public static void liftToTopTextLayer(Matrix3x2fStack stack) {
    }

    public static float[] transformPoint(Matrix3x2fStack stack, float x, float y) {
        Vector2f transformed = new Vector2f();
        stack.transformPosition(x, y, transformed);
        return new float[] { transformed.x, transformed.y };
    }
}
