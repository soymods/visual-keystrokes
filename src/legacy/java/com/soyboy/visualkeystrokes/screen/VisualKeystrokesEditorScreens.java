package com.soyboy.visualkeystrokes.screen;

import com.soyboy.visualkeystrokes.config.OverlayConfig;
import net.minecraft.client.gui.screen.Screen;

public final class VisualKeystrokesEditorScreens {
    private VisualKeystrokesEditorScreens() {
    }

    public static Screen createEditorScreen(OverlayConfig config) {
        return new VisualKeystrokesEditorScreenLegacy(config);
    }
}
