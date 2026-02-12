package com.ryduzz.visualkeystrokes.render;

import com.ryduzz.visualkeystrokes.config.OverlayConfig;
import com.ryduzz.visualkeystrokes.input.InputTracker;
import com.ryduzz.visualkeystrokes.ui.UiStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import com.ryduzz.visualkeystrokes.util.MatrixStackCompat;
import com.ryduzz.visualkeystrokes.util.RenderSnap;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Supplier;

public final class KeystrokeOverlayRenderer {
    private static final float KEY_TEXT_BASE_SCALE = 0.84f;
    private static final float KEY_TEXT_BASE_HEIGHT = 24.0f;
    private static final float MOUSE_LABEL_SCALE_MULTIPLIER = 0.90f;
    private final InputTracker tracker;
    private final Supplier<OverlayConfig> configSupplier;
    private final MinecraftClient client;

    public KeystrokeOverlayRenderer(InputTracker tracker, Supplier<OverlayConfig> configSupplier) {
        this.tracker = tracker;
        this.configSupplier = configSupplier;
        this.client = MinecraftClient.getInstance();
    }

    public void render(DrawContext context) {
        OverlayConfig config = configSupplier.get();
        TextRenderer textRenderer = client.textRenderer;
        float renderScale = RenderSnap.snapScale(config.scale);
        double offsetX = RenderSnap.snapOffset(config.offsetX, renderScale);
        double offsetY = RenderSnap.snapOffset(config.offsetY, renderScale);

        MatrixStackCompat.push(context.getMatrices());
        MatrixStackCompat.translate(context.getMatrices(), offsetX, offsetY);
        MatrixStackCompat.scale(context.getMatrices(), renderScale, renderScale);

        for (OverlayConfig.KeyDefinition key : config.keys) {
            if (!key.isVisible()) {
                continue;
            }
            boolean pressed = tracker.isPressed(key.type, key.code);
            int background = OverlayConfig.resolveColor(key.backgroundColorOverride, config.backgroundColor);
            int pressedBackground = OverlayConfig.resolveColor(key.pressedColorOverride, config.pressedColor);
            int borderColor = OverlayConfig.resolveColor(key.borderColorOverride, config.borderColor);
            int textColor = OverlayConfig.resolveColor(key.textColorOverride, config.textColor);
            float pressedOpacity = key.pressedOpacityOverride == null ? config.pressedOpacity : key.pressedOpacityOverride;
            pressedOpacity = Math.max(0.0f, Math.min(1.0f, pressedOpacity));
            int fillColor = pressed ? lerpColor(background, pressedBackground, pressedOpacity) : background;

            int x = key.x;
            int y = key.y;
            int width = key.width;
            int height = key.height;

            UiStyle.drawKeyCap(context, x, y, width, height, fillColor, borderColor, pressed);

            if (key.type == OverlayConfig.InputType.STAT) {
                String value = tracker.getStatValue(key.statId);
                int halfHeight = Math.max(1, height / 2);
                drawScaledTextFitBox(context, key.label, x + 2, y + 1, width - 4, halfHeight - 1, textColor);
                drawScaledTextFitBox(context, value, x + 2, y + halfHeight, width - 4, height - halfHeight - 1, textColor);
            } else {
                drawScaledTextFitBox(context, key.label, x + 2, y + 1, width - 4, height - 2, textColor);
            }
        }

        MatrixStackCompat.pop(context.getMatrices());
    }

    private void drawScaledTextFitBox(DrawContext context, String text, int boxX, int boxY, int boxWidth, int boxHeight, int color) {
        TextRenderer textRenderer = client.textRenderer;
        int textWidth = textRenderer.getWidth(text);
        if (textWidth <= 0 || boxWidth <= 0 || boxHeight <= 0) {
            return;
        }

        float scale = KEY_TEXT_BASE_SCALE * (Math.max(1.0f, boxHeight) / KEY_TEXT_BASE_HEIGHT);
        if ("LMB".equals(text) || "RMB".equals(text)) {
            scale *= MOUSE_LABEL_SCALE_MULTIPLIER;
        }
        scale = Math.max(0.70f, Math.min(3.0f, scale));

        if (scale <= 0.999f) {
            float centerX = boxX + boxWidth / 2.0f;
            float centerY = boxY + boxHeight / 2.0f;
            float drawX = centerX - (textWidth * scale) / 2.0f;
            float drawY = centerY - (textRenderer.fontHeight * scale) / 2.0f;
            MatrixStackCompat.push(context.getMatrices());
            MatrixStackCompat.translate(context.getMatrices(), drawX, drawY);
            MatrixStackCompat.scale(context.getMatrices(), scale, scale);
            context.drawTextWithShadow(textRenderer, text, 0, 0, color);
            MatrixStackCompat.pop(context.getMatrices());
            return;
        }

        if (scale <= 1.001f) {
            int textX = boxX + (boxWidth - textWidth) / 2;
            int textY = boxY + (boxHeight - textRenderer.fontHeight) / 2;
            context.drawTextWithShadow(textRenderer, text, textX, textY, color);
            return;
        }

        float centerX = boxX + boxWidth / 2.0f;
        float centerY = boxY + boxHeight / 2.0f;
        float drawX = centerX - (textWidth * scale) / 2.0f;
        float drawY = centerY - (textRenderer.fontHeight * scale) / 2.0f;
        MatrixStackCompat.push(context.getMatrices());
        MatrixStackCompat.translate(context.getMatrices(), drawX, drawY);
        MatrixStackCompat.scale(context.getMatrices(), scale, scale);
        context.drawTextWithShadow(textRenderer, text, 0, 0, color);
        MatrixStackCompat.pop(context.getMatrices());
    }

    private static int lerpColor(int from, int to, float progress) {
        float t = Math.max(0.0f, Math.min(1.0f, progress));
        int aFrom = (from >>> 24) & 0xFF;
        int rFrom = (from >>> 16) & 0xFF;
        int gFrom = (from >>> 8) & 0xFF;
        int bFrom = from & 0xFF;

        int aTo = (to >>> 24) & 0xFF;
        int rTo = (to >>> 16) & 0xFF;
        int gTo = (to >>> 8) & 0xFF;
        int bTo = to & 0xFF;

        int a = (int) (aFrom + (aTo - aFrom) * t);
        int r = (int) (rFrom + (rTo - rFrom) * t);
        int g = (int) (gFrom + (gTo - gFrom) * t);
        int b = (int) (bFrom + (bTo - bFrom) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
