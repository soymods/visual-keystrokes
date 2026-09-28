package com.soyboy.visualkeystrokes.fabric;

import com.soyboy.visualkeystrokes.VisualKeystrokesClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;

public final class VisualKeystrokesFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VisualKeystrokesClient.init(KeyBindingHelper::registerKeyBinding);

        HudRenderCallback.EVENT.register((context, tickDelta) -> VisualKeystrokesClient.onHudRender(context));
        ClientTickEvents.END_CLIENT_TICK.register(client -> VisualKeystrokesClient.onEndClientTick());
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) ->
            VisualKeystrokesClient.onScreenInit(
                client,
                screen,
                scaledWidth,
                scaledHeight,
                Screens.getButtons(screen),
                Screens.getButtons(screen)::add
            )
        );
    }
}
