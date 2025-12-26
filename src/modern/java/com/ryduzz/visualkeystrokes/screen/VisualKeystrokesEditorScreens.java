package com.ryduzz.visualkeystrokes.screen;

import com.ryduzz.visualkeystrokes.config.OverlayConfig;
import net.minecraft.client.gui.screen.Screen;

import java.lang.reflect.Method;

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
        try {
            ClassLoader loader = VisualKeystrokesEditorScreens.class.getClassLoader();
            Class<?> elementClass = Class.forName("net.minecraft.client.gui.Element", false, loader);
            for (Method method : elementClass.getMethods()) {
                if (!method.getName().equals("mouseClicked") || method.getParameterCount() != 2) {
                    continue;
                }
                Class<?>[] params = method.getParameterTypes();
                if ("net.minecraft.client.gui.Click".equals(params[0].getName())) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
