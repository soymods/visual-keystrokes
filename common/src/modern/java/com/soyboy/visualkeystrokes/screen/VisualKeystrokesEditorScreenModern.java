package com.soyboy.visualkeystrokes.screen;

import com.soyboy.visualkeystrokes.config.OverlayConfig;
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
        boolean primaryClick = isPrimaryClick(click);
        if (primaryClick && searchField != null && isSearchFieldHit(click.x(), click.y())) {
            setFocused(searchField);
            searchField.setFocused(true);
            searchField.onClick(click, doubleClick);
            return true;
        }

        if (handleMouseClicked(
            click.x(),
            click.y(),
            primaryClick ? GLFW.GLFW_MOUSE_BUTTON_LEFT : click.button(),
            doubleClick
        )) {
            return true;
        }
        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        boolean primaryClick = isPrimaryClick(click);
        if (handleMouseDragged(
            click.x(),
            click.y(),
            primaryClick ? GLFW.GLFW_MOUSE_BUTTON_LEFT : click.button(),
            deltaX,
            deltaY
        )) {
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        boolean primaryClick = isPrimaryClick(click);
        if (handleMouseReleased(
            click.x(),
            click.y(),
            primaryClick ? GLFW.GLFW_MOUSE_BUTTON_LEFT : click.button()
        )) {
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

    private static boolean isPrimaryClick(Click click) {
        return click.isLeft() || click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT;
    }
}
