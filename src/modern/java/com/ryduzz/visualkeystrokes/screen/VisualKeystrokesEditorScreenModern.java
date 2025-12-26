package com.ryduzz.visualkeystrokes.screen;

import com.ryduzz.visualkeystrokes.config.OverlayConfig;
import net.minecraft.client.gui.Click;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

public final class VisualKeystrokesEditorScreenModern extends VisualKeystrokesEditorScreenBase {
    public VisualKeystrokesEditorScreenModern(OverlayConfig config) {
        super(config);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && searchField != null && isSearchFieldHit(click.x(), click.y())) {
            setFocused(searchField);
            searchField.setFocused(true);
            searchField.onClick(click, doubleClick);
            return true;
        }
        if (handleMouseClicked(click.x(), click.y(), click.button(), doubleClick)) {
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (handleMouseDragged(click.x(), click.y(), click.button(), deltaX, deltaY)) {
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (handleMouseReleased(click.x(), click.y(), click.button())) {
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (searchField != null && searchField.isFocused()) {
            if (searchField.keyPressed(input)) {
                return true;
            }
            if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
                searchField.setFocused(false);
                setFocused(null);
                return true;
            }
        }
        if (handleKeyPressedCommon(input.key())) {
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchField != null && searchField.isFocused() && searchField.charTyped(input)) {
            return true;
        }
        return super.charTyped(input);
    }
}
