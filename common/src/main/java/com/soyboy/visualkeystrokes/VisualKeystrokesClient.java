package com.soyboy.visualkeystrokes;

import com.soyboy.visualkeystrokes.config.OverlayConfig;
import com.soyboy.visualkeystrokes.input.InputTracker;
import com.soyboy.visualkeystrokes.render.KeystrokeOverlayRenderer;
import com.soyboy.visualkeystrokes.screen.VisualKeystrokesEditor;
import com.soyboy.visualkeystrokes.screen.VisualKeystrokesEditorScreens;
import com.soyboy.visualkeystrokes.screen.VisualKeystrokesMainMenuIntegration;
import com.soyboy.visualkeystrokes.util.KeyBindingCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

/**
 * Loader-agnostic client logic. The Fabric and NeoForge entrypoints call {@link #init} once and
 * forward their HUD, tick, and screen events to the {@code on*} hooks.
 */
public final class VisualKeystrokesClient {
    public static final String MOD_ID = "visualkeystrokes";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static OverlayConfig config;
    private static InputTracker tracker;
    private static KeystrokeOverlayRenderer renderer;
    private static KeyBinding toggleKey;

    private VisualKeystrokesClient() {
    }

    /**
     * @param keyRegistrar registers a key binding with the current loader
     */
    public static void init(Consumer<KeyBinding> keyRegistrar) {
        config = OverlayConfig.loadOrCreate();

        try {
            KeyBinding key = KeyBindingCompat.createKeyBinding(
                "key.visualkeystrokes.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F8
            );
            keyRegistrar.accept(key);
            toggleKey = key;
        } catch (Throwable t) {
            LOGGER.error("Failed to register VisualKeystrokes toggle keybinding; startup will continue with keybind disabled.", t);
        }
    }

    // NeoForge constructs mods before MinecraftClient exists, so the pieces that hold on to the
    // client are built on first use instead of in init.
    private static void ensureClientReady() {
        if (tracker == null) {
            tracker = new InputTracker(MinecraftClient.getInstance());
            renderer = new KeystrokeOverlayRenderer(tracker, () -> config);
        }
    }

    public static void onHudRender(DrawContext context) {
        ensureClientReady();
        if (config.enabled && !(MinecraftClient.getInstance().currentScreen instanceof VisualKeystrokesEditor)) {
            renderer.render(context);
        }
    }

    public static void onEndClientTick() {
        ensureClientReady();
        tracker.update();
        if (toggleKey == null) {
            return;
        }
        while (toggleKey.wasPressed()) {
            config.enabled = !config.enabled;
            OverlayConfig.save(config);
        }
    }

    /**
     * @param children   the screen's current widgets, used to place buttons below existing ones
     * @param addWidget  adds a widget to the screen (rendered, clickable, and narrated)
     */
    public static void onScreenInit(
        MinecraftClient client,
        Screen screen,
        int scaledWidth,
        int scaledHeight,
        List<? extends Element> children,
        Consumer<ClickableWidget> addWidget
    ) {
        if (screen instanceof TitleScreen) {
            VisualKeystrokesMainMenuIntegration.addButton(client, addWidget, () -> config);
            return;
        }
        if (!(screen instanceof GameMenuScreen)) {
            return;
        }

        int buttonWidth = 204;
        int buttonHeight = 20;
        int x = scaledWidth / 2 - buttonWidth / 2;
        int y = scaledHeight / 4 + 96;

        for (Element child : children) {
            if (child instanceof ClickableWidget button) {
                y = Math.max(y, button.getY() + button.getHeight() + 4);
            }
        }

        if (y + buttonHeight > scaledHeight - 24) {
            y = scaledHeight - 24 - buttonHeight;
        }

        addWidget.accept(ButtonWidget.builder(Text.literal("Edit Keystrokes"), button ->
            client.setScreen(VisualKeystrokesEditorScreens.createEditorScreen(config))
        ).dimensions(x, y, buttonWidth, buttonHeight).build());
    }
}
