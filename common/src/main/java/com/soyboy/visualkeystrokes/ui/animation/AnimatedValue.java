package com.soyboy.visualkeystrokes.ui.animation;

import java.util.function.Function;

public class AnimatedValue {

    private float currentValue;
    private float targetValue;
    private float startValue;
    private long animationStartTime;
    private int durationMs;
    private boolean animating;
    private Function<Float, Float> easingFunction;

    public AnimatedValue(float initialValue, Function<Float, Float> easingFunction) {
        this.currentValue = initialValue;
        this.targetValue = initialValue;
        this.startValue = initialValue;
        this.animating = false;
        this.easingFunction = easingFunction;
    }

    public void animateTo(float target, int durationMs) {
        if (Math.abs(target - this.targetValue) < 0.001f) {
            if (this.animating) {
                return;
            }
            if (Math.abs(target - this.currentValue) < 0.001f) {
                return;
            }
        }
        if (durationMs <= 0) {
            setValue(target);
            return;
        }
        this.startValue = this.currentValue;
        this.targetValue = target;
        this.durationMs = durationMs;
        this.animationStartTime = System.currentTimeMillis();
        this.animating = true;
    }

    public void tick() {
        if (!animating) {
            return;
        }
        long elapsed = System.currentTimeMillis() - animationStartTime;
        if (elapsed >= durationMs) {
            currentValue = targetValue;
            animating = false;
            return;
        }
        float rawProgress = (float) elapsed / durationMs;
        float easedProgress = easingFunction.apply(rawProgress);
        currentValue = AnimationHelper.lerp(startValue, targetValue, easedProgress);
    }

    public float getValue() {
        return currentValue;
    }

    public void setValue(float value) {
        this.currentValue = value;
        this.targetValue = value;
        this.startValue = value;
        this.animating = false;
    }

    public boolean isAnimating() {
        return animating;
    }

    public boolean isAtTarget() {
        return Math.abs(currentValue - targetValue) < 0.001f;
    }
}
