package com.ryduzz.visualkeystrokes.screen;

import com.ryduzz.visualkeystrokes.config.OverlayConfig;
import net.minecraft.client.gui.screen.Screen;

public final class VisualKeystrokesEditorScreens {
    private VisualKeystrokesEditorScreens() {
    }

    public static Screen createEditorScreen(OverlayConfig config) {
        return new VisualKeystrokesEditorScreenLegacy(config);
    }
}
