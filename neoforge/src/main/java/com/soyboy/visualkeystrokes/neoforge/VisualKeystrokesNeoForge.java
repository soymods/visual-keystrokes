package com.soyboy.visualkeystrokes.neoforge;

import com.soyboy.visualkeystrokes.VisualKeystrokesClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

@Mod(value = VisualKeystrokesClient.MOD_ID, dist = Dist.CLIENT)
public final class VisualKeystrokesNeoForge {
    private static final Logger LOGGER = LoggerFactory.getLogger(VisualKeystrokesClient.MOD_ID);

    public VisualKeystrokesNeoForge(IEventBus modEventBus) {
        VisualKeystrokesClient.init(key ->
            modEventBus.addListener(RegisterKeyMappingsEvent.class, event -> registerKeyBinding(event, key))
        );

        NeoForge.EVENT_BUS.addListener(RenderGuiEvent.Post.class, event ->
            VisualKeystrokesClient.onHudRender(event.getGuiGraphics())
        );
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event ->
            VisualKeystrokesClient.onEndClientTick()
        );
        NeoForge.EVENT_BUS.addListener(ScreenEvent.Init.Post.class, event -> {
            Screen screen = event.getScreen();
            VisualKeystrokesClient.onScreenInit(
                MinecraftClient.getInstance(),
                screen,
                screen.width,
                screen.height,
                event.getListenersList(),
                event::addListener
            );
        });
    }

    private static void registerKeyBinding(RegisterKeyMappingsEvent event, KeyBinding key) {
        registerCategory(event, key.getCategory());
        event.register(key);
    }

    // 1.21.9+ uses KeyBinding.Category objects that NeoForge wants registered through the event.
    // Older versions use plain string categories and have no registerCategory method.
    private static void registerCategory(RegisterKeyMappingsEvent event, Object category) {
        if (isInVanillaSortOrder(category)) {
            return;
        }
        for (Method method : event.getClass().getMethods()) {
            if (!"registerCategory".equals(method.getName()) || method.getParameterCount() != 1) {
                continue;
            }
            if (!method.getParameterTypes()[0].isInstance(category)) {
                continue;
            }
            try {
                method.invoke(event, category);
            } catch (ReflectiveOperationException e) {
                LOGGER.warn("Failed to register VisualKeystrokes key category {}", category, e);
            }
            return;
        }
    }

    // In the Yarn dev environment KeyBindingCompat finds vanilla's Category.create, which already adds
    // the category to vanilla's sort order (its only static List); NeoForge would append it again.
    private static boolean isInVanillaSortOrder(Object category) {
        for (Field field : category.getClass().getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || !List.class.isAssignableFrom(field.getType())) {
                continue;
            }
            try {
                field.setAccessible(true);
                return ((List<?>) field.get(null)).contains(category);
            } catch (ReflectiveOperationException | RuntimeException e) {
                return false;
            }
        }
        return false;
    }
}
