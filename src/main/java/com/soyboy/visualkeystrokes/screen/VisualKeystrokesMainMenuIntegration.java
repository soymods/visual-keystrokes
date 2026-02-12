package com.soyboy.visualkeystrokes.screen;

import com.soyboy.visualkeystrokes.config.OverlayConfig;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.TitleScreen;

public final class VisualKeystrokesMainMenuIntegration {
    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_MARGIN = 8;
    private static final int BUTTON_SPACING = 4;
    private static final int DEFAULT_BUTTON_INDEX = 0;
    private static final int BELOW_PATHMIND_BUTTON_INDEX = 1;

    private VisualKeystrokesMainMenuIntegration() {
    }

    public static void register(Supplier<OverlayConfig> configSupplier) {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof TitleScreen)) {
                return;
            }

            int x = BUTTON_MARGIN;
            int y = BUTTON_MARGIN + resolveButtonIndex() * (BUTTON_SIZE + BUTTON_SPACING);

            Screens.getButtons(screen).add(new VisualKeystrokesMainMenuButton(x, y, BUTTON_SIZE, button ->
                client.setScreen(VisualKeystrokesEditorScreens.createEditorScreen(configSupplier.get()))
            ));
        });
    }

    private static int resolveButtonIndex() {
        return FabricLoader.getInstance().isModLoaded("pathmind")
            ? BELOW_PATHMIND_BUTTON_INDEX
            : DEFAULT_BUTTON_INDEX;
    }
}
