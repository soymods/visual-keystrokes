package com.soyboy.visualkeystrokes.util;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector4f;

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

    public static void liftToTopTextLayer(MatrixStack stack) {
        stack.translate(0.0d, 0.0d, 200.0d);
    }

    public static float[] transformPoint(MatrixStack stack, float x, float y) {
        Vector4f transformed = new Vector4f(x, y, 0.0f, 1.0f).mul(stack.peek().getPositionMatrix());
        return new float[] { transformed.x(), transformed.y() };
    }
}
