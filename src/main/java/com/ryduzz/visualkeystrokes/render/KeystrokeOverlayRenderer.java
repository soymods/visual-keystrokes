package com.ryduzz.visualkeystrokes.render;

import com.ryduzz.visualkeystrokes.config.OverlayConfig;
import com.ryduzz.visualkeystrokes.input.InputTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Supplier;

public final class KeystrokeOverlayRenderer {
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

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(config.offsetX, config.offsetY);
        context.getMatrices().scale(config.scale, config.scale);

        for (OverlayConfig.KeyDefinition key : config.keys) {
            if (!key.isVisible()) {
                continue;
            }
            boolean pressed = tracker.isPressed(key.type, key.code);
            int background = pressed ? config.pressedColor : config.backgroundColor;

            int x = key.x;
            int y = key.y;
            int width = key.width;
            int height = key.height;

            context.fill(x, y, x + width, y + height, background);
            drawBorder(context, x, y, width, height, config.borderColor);

            if (key.type == OverlayConfig.InputType.STAT) {
                String value = tracker.getStatValue(key.statId);
                int labelWidth = textRenderer.getWidth(key.label);
                int labelX = x + (width - labelWidth) / 2;
                int labelY = y + 2;
                context.drawTextWithShadow(textRenderer, key.label, labelX, labelY, config.textColor);

                int valueWidth = textRenderer.getWidth(value);
                int valueX = x + (width - valueWidth) / 2;
                int valueY = y + height - textRenderer.fontHeight - 2;
                context.drawTextWithShadow(textRenderer, value, valueX, valueY, config.textColor);
            } else {
                int textWidth = textRenderer.getWidth(key.label);
                int textX = x + (width - textWidth) / 2;
                int textY = y + (height - textRenderer.fontHeight) / 2;
                context.drawTextWithShadow(textRenderer, key.label, textX, textY, config.textColor);
            }
        }

        context.getMatrices().popMatrix();
    }

    private static void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 1, color);
        context.fill(x, y + height - 1, x + width, y + height, color);
        context.fill(x, y, x + 1, y + height, color);
        context.fill(x + width - 1, y, x + width, y + height, color);
    }
}
