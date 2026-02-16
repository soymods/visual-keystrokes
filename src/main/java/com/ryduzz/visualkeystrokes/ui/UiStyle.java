package com.ryduzz.visualkeystrokes.ui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class UiStyle {
    public static final int BG_BASE = 0xFF0B0D11;
    public static final int BG_SHADE = 0xCC07090D;
    public static final int PANEL_OUTER = 0xFF090B0F;
    public static final int PANEL_FILL = 0xE01A1D24;
    public static final int PANEL_INSET = 0xCC11141A;
    public static final int BUTTON_FILL = 0xFF1F232C;
    public static final int BUTTON_HOVER = 0xFF2A2F3A;
    public static final int BUTTON_ACTIVE = 0xFF3B3524;
    public static final int TEXT_PRIMARY = 0xFFE6EAF0;
    public static final int TEXT_MUTED = 0xFF838A96;
    public static final int EDGE_DARK = 0xFF000000;
    public static final int EDGE_LIGHT = 0xFF3A3F4C;
    public static final int ACCENT_GOLD = 0xFFE4B93B;
    public static final int ACCENT_GREEN = 0xFF6CD26B;
    public static final int ACCENT_RED = 0xFFE15656;
    public static final int ACCENT_BLUE = 0xFF58B6E6;

    private UiStyle() {
    }

    public static void drawBackdrop(DrawContext context, int width, int height) {
        context.fill(0, 0, width, height, BG_BASE);
        context.fill(0, 0, width, height, BG_SHADE);
        for (int y = 0; y < height; y += 3) {
            context.fill(0, y, width, y + 1, 0x14000000);
        }
    }

    public static void drawPanel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, PANEL_OUTER);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_FILL);
        drawFrame(context, x, y, width, height, EDGE_LIGHT, EDGE_DARK);
    }

    public static void drawInset(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, PANEL_INSET);
        drawFrame(context, x, y, width, height, EDGE_DARK, EDGE_LIGHT);
    }

    public static void drawSectionHeader(
        DrawContext context,
        TextRenderer textRenderer,
        String label,
        int x,
        int y,
        int width,
        int accentColor
    ) {
        int height = textRenderer.fontHeight + 6;
        context.fill(x, y, x + width, y + height, 0xFF10141B);
        drawFrame(context, x, y, width, height, EDGE_LIGHT, EDGE_DARK);
        context.fill(x + 2, y + 2, x + 6, y + height - 2, accentColor);
        context.drawTextWithShadow(textRenderer, label, x + 10, y + 3, TEXT_PRIMARY);
    }

    public static int drawButton(
        DrawContext context,
        TextRenderer textRenderer,
        String label,
        int x,
        int y,
        int width,
        int height,
        boolean hovered,
        boolean active,
        int accentColor
    ) {
        int fill = active ? BUTTON_ACTIVE : (hovered ? BUTTON_HOVER : BUTTON_FILL);
        context.fill(x, y, x + width, y + height, fill);
        drawFrame(context, x, y, width, height, EDGE_LIGHT, EDGE_DARK);
        if (active) {
            context.fill(x + 2, y + 2, x + width - 2, y + 3, accentColor);
            context.fill(x + 2, y + height - 3, x + width - 2, y + height - 2, accentColor);
        }
        int textWidth = textRenderer.getWidth(label);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - textRenderer.fontHeight) / 2;
        context.drawTextWithShadow(textRenderer, label, textX, textY, TEXT_PRIMARY);
        return fill;
    }

    public static void drawToggle(DrawContext context, int x, int y, int width, int height, boolean enabled, boolean hovered) {
        int accent = enabled ? ACCENT_GREEN : ACCENT_RED;
        int fill = hovered ? BUTTON_HOVER : BUTTON_FILL;
        context.fill(x, y, x + width, y + height, fill);
        drawFrame(context, x, y, width, height, EDGE_LIGHT, EDGE_DARK);

        int knobSize = Math.max(6, height - 4);
        int knobX = enabled ? x + width - knobSize - 2 : x + 2;
        int knobY = y + 2;
        context.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, accent);
        drawFrame(context, knobX, knobY, knobSize, knobSize, 0xFFFFFFFF, EDGE_DARK);
    }

    public static void drawKeyCap(
        DrawContext context,
        int x,
        int y,
        int width,
        int height,
        int background,
        int border,
        boolean pressed
    ) {
        int base = pressed ? tint(background, 18) : background;
        int shade = pressed ? tint(base, -12) : tint(base, -18);
        context.fill(x, y, x + width, y + height, base);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, shade);
        drawFrame(context, x, y, width, height, tint(border, 40), tint(border, -60));
    }

    public static void drawFrame(DrawContext context, int x, int y, int width, int height, int light, int dark) {
        context.fill(x, y, x + width, y + 1, light);
        context.fill(x, y, x + 1, y + height, light);
        context.fill(x, y + height - 1, x + width, y + height, dark);
        context.fill(x + width - 1, y, x + width, y + height, dark);
    }

    public static int tint(int color, int amount) {
        int a = (color >>> 24) & 0xFF;
        int r = clamp(((color >>> 16) & 0xFF) + amount);
        int g = clamp(((color >>> 8) & 0xFF) + amount);
        int b = clamp((color & 0xFF) + amount);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        if (value > 255) {
            return 255;
        }
        return value;
    }
}
