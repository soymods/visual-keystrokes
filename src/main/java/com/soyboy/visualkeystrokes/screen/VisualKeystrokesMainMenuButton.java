package com.soyboy.visualkeystrokes.screen;

import com.soyboy.visualkeystrokes.ui.UiStyle;
import com.soyboy.visualkeystrokes.util.TextCompatibilityBridge;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Small title-screen icon button used to open the Visual Keystrokes editor.
 */
public final class VisualKeystrokesMainMenuButton extends ButtonWidget {
    private static final Identifier ICON_TEXTURE = Identifier.of("visualkeystrokes", "textures/gui/button_logo.png");
    private static final int ICON_PADDING = 2;
    private static final int SHADOW_COLOR = 0x66000000;
    private static final String OPEN_EDITOR_KEY = "gui.visualkeystrokes.open_editor";
    private static final String OPEN_EDITOR_FALLBACK = "Open Visual Keystrokes Editor";

    public VisualKeystrokesMainMenuButton(int x, int y, int size, PressAction pressAction) {
        super(x, y, size, size, TextCompatibilityBridge.empty(), pressAction, DEFAULT_NARRATION_SUPPLIER);
        this.setTooltip(Tooltip.of(resolveOpenEditorText()));
    }

    // 1.21.11+ calls drawIcon from final renderWidget.
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
        renderButtonIcon(context);
    }

    // 1.21.10 and below call drawMessage from renderWidget.
    public void drawMessage(DrawContext context, TextRenderer textRenderer, int color) {
        renderButtonIcon(context);
    }

    @Override
    protected MutableText getNarrationMessage() {
        return TextCompatibilityBridge.copy(resolveOpenEditorText());
    }

    private void renderButtonIcon(DrawContext context) {
        this.setTooltip(Tooltip.of(resolveOpenEditorText()));

        boolean hovered = this.active && this.isHovered();
        int baseFill = !this.active
            ? 0xFF252A33
            : (hovered ? 0xFF223A5A : 0xFF162842);
        int fill = applyAlpha(baseFill, this.alpha);
        int borderTop = applyAlpha(hovered ? 0xFFFFFFFF : UiStyle.EDGE_LIGHT, this.alpha);
        int borderBottom = applyAlpha(hovered ? 0xFFFFFFFF : UiStyle.EDGE_DARK, this.alpha);
        int shadow = applyAlpha(SHADOW_COLOR, this.alpha);

        int x = this.getX();
        int y = this.getY();
        int w = this.width;
        int h = this.height;
        context.fill(x + 1, y + h, x + w + 1, y + h + 1, shadow);
        context.fill(x + w, y + 1, x + w + 1, y + h, shadow);
        context.fill(x, y, x + w, y + h, fill);
        context.fill(x, y, x + w, y + 1, borderTop);
        context.fill(x, y, x + 1, y + h, borderTop);
        context.fill(x, y + h - 1, x + w, y + h, borderBottom);
        context.fill(x + w - 1, y, x + w, y + h, borderBottom);

        int iconSize = this.width - ICON_PADDING * 2;
        int iconX = x + ICON_PADDING;
        int iconY = y + ICON_PADDING;

        int rgb;
        if (!this.active) {
            rgb = 0xA0A0A0;
        } else {
            rgb = 0xFFFFFF;
        }

        int alphaComponent = MathHelper.ceil(this.alpha * 255.0F);
        if (alphaComponent <= 0) {
            return;
        }
        int tint = (alphaComponent << 24) | rgb;
        GuiTextureRenderer.drawIcon(context, ICON_TEXTURE, iconX, iconY, iconSize, tint);
    }

    private static int applyAlpha(int color, float alphaMultiplier) {
        int alpha = (color >>> 24) & 0xFF;
        int scaledAlpha = MathHelper.ceil(alpha * Math.max(0.0f, Math.min(1.0f, alphaMultiplier)));
        return (scaledAlpha << 24) | (color & 0x00FFFFFF);
    }

    private static MutableText resolveOpenEditorText() {
        return TextCompatibilityBridge.translatableWithFallback(OPEN_EDITOR_KEY, OPEN_EDITOR_FALLBACK);
    }
}
