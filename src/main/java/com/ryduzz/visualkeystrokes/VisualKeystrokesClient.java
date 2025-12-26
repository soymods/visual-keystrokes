package com.ryduzz.visualkeystrokes;

import com.ryduzz.visualkeystrokes.config.OverlayConfig;
import com.ryduzz.visualkeystrokes.input.InputTracker;
import com.ryduzz.visualkeystrokes.render.KeystrokeOverlayRenderer;
import com.ryduzz.visualkeystrokes.screen.VisualKeystrokesEditor;
import com.ryduzz.visualkeystrokes.screen.VisualKeystrokesEditorScreens;
import com.ryduzz.visualkeystrokes.util.KeyBindingCompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class VisualKeystrokesClient implements ClientModInitializer {
    private static OverlayConfig config;
    @Override
    public void onInitializeClient() {
        config = OverlayConfig.loadOrCreate();

        MinecraftClient client = MinecraftClient.getInstance();
        InputTracker tracker = new InputTracker(client);
        KeystrokeOverlayRenderer renderer = new KeystrokeOverlayRenderer(tracker, () -> config);

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (config.enabled && !(client.currentScreen instanceof VisualKeystrokesEditor)) {
                renderer.render(context);
            }
        });

        KeyBinding toggleKey = KeyBindingHelper.registerKeyBinding(
            KeyBindingCompat.createKeyBinding(
                "key.visualkeystrokes.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(tickClient -> {
            tracker.update();
            while (toggleKey.wasPressed()) {
                config.enabled = !config.enabled;
                OverlayConfig.save(config);
            }
        });

        ScreenEvents.AFTER_INIT.register((screenClient, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof GameMenuScreen)) {
                return;
            }

            int buttonWidth = 204;
            int buttonHeight = 20;
            int x = scaledWidth / 2 - buttonWidth / 2;
            int y = scaledHeight / 4 + 96;

            for (var button : Screens.getButtons(screen)) {
                y = Math.max(y, button.getY() + button.getHeight() + 4);
            }

            if (y + buttonHeight > scaledHeight - 24) {
                y = scaledHeight - 24 - buttonHeight;
            }

            Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("Edit Keystrokes"), button ->
                screenClient.setScreen(VisualKeystrokesEditorScreens.createEditorScreen(config))
            ).dimensions(x, y, buttonWidth, buttonHeight).build());
        });
    }
}
