package com.soyboy.visualkeystrokes.screen;

import com.soyboy.visualkeystrokes.config.OverlayConfig;
import com.soyboy.visualkeystrokes.platform.VisualKeystrokesPlatform;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;

public final class VisualKeystrokesMainMenuIntegration {
    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_MARGIN = 8;
    private static final int BUTTON_SPACING = 4;
    private static final int DEFAULT_BUTTON_INDEX = 0;
    private static final int BELOW_PATHMIND_BUTTON_INDEX = 1;

    private VisualKeystrokesMainMenuIntegration() {
    }

    public static void addButton(MinecraftClient client, Consumer<ClickableWidget> addWidget, Supplier<OverlayConfig> configSupplier) {
        int x = BUTTON_MARGIN;
        int y = BUTTON_MARGIN + resolveButtonIndex() * (BUTTON_SIZE + BUTTON_SPACING);

        addWidget.accept(new VisualKeystrokesMainMenuButton(x, y, BUTTON_SIZE, button ->
            client.setScreen(VisualKeystrokesEditorScreens.createEditorScreen(configSupplier.get()))
        ));
    }

    private static int resolveButtonIndex() {
        return VisualKeystrokesPlatform.isModLoaded("pathmind")
            ? BELOW_PATHMIND_BUTTON_INDEX
            : DEFAULT_BUTTON_INDEX;
    }
}
