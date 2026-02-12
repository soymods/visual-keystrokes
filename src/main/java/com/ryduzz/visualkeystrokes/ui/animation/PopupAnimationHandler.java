package com.ryduzz.visualkeystrokes.ui.animation;

public class PopupAnimationHandler {

    private static final int OPEN_DURATION_MS = 220;
    private static final int BACKGROUND_CLOSE_DURATION_MS = 180;
    private static final int POPUP_CLOSE_DURATION_MS = 160;

    private final AnimatedValue backgroundAlpha;
    private final AnimatedValue popupScale;
    private boolean visible;
    private boolean targetVisible;

    public PopupAnimationHandler() {
        this.backgroundAlpha = new AnimatedValue(0f, AnimationHelper::easeOutCubic);
        this.popupScale = new AnimatedValue(0f, AnimationHelper::easeOutCubic);
    }

    public void show() {
        targetVisible = true;
        visible = true;
        backgroundAlpha.animateTo(1f, OPEN_DURATION_MS);
        popupScale.animateTo(1f, OPEN_DURATION_MS);
    }

    public void hide() {
        targetVisible = false;
        backgroundAlpha.animateTo(0f, BACKGROUND_CLOSE_DURATION_MS);
        popupScale.animateTo(0f, POPUP_CLOSE_DURATION_MS);
    }

    public void tick() {
        backgroundAlpha.tick();
        popupScale.tick();
        if (!targetVisible && !isAnimating() && backgroundAlpha.isAtTarget() && popupScale.isAtTarget()) {
            visible = false;
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isAnimating() {
        return backgroundAlpha.isAnimating() || popupScale.isAnimating();
    }

    public boolean isFullyVisible() {
        return visible && !isAnimating() && backgroundAlpha.isAtTarget();
    }

    public float getPopupProgress() {
        return popupScale.getValue();
    }

    public int getAnimatedBackgroundColor(int baseColor) {
        float animationProgress = backgroundAlpha.getValue();
        int originalAlpha = (baseColor >>> 24) & 0xFF;
        int animatedAlpha = (int) (originalAlpha * animationProgress);
        return (animatedAlpha << 24) | (baseColor & 0x00FFFFFF);
    }
}
