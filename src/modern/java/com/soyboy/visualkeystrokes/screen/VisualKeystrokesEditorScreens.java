package com.soyboy.visualkeystrokes.screen;

import com.soyboy.visualkeystrokes.config.OverlayConfig;
import net.minecraft.client.gui.screen.Screen;


public final class VisualKeystrokesEditorScreens {
    private static final boolean MODERN_INPUT_AVAILABLE = hasModernInput();

    private VisualKeystrokesEditorScreens() {
    }

    public static Screen createEditorScreen(OverlayConfig config) {
        if (MODERN_INPUT_AVAILABLE) {
            return new VisualKeystrokesEditorScreenModern(config);
        }
        return new VisualKeystrokesEditorScreenLegacy(config);
    }

    private static boolean hasModernInput() {
        // Checking Minecraft class names (e.g. "net.minecraft.client.gui.Element") by string
        // does not work in production because Fabric Loom remaps bytecode references but NOT
        // string constants, so at runtime all MC classes have intermediary names (class_XXXX).
        // Instead, probe for the mod's own modern screen class — mod package names are never
        // remapped — and let a ClassNotFoundException or NoClassDefFoundError signal that the
        // required MC API (net.minecraft.client.gui.Click) is unavailable.
        try {
            Class.forName(
                "com.soyboy.visualkeystrokes.screen.VisualKeystrokesEditorScreenModern",
                false,
                VisualKeystrokesEditorScreens.class.getClassLoader()
            );
            return true;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return false;
        }
    }
}
