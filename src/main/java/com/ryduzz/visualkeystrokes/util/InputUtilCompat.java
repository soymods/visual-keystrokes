package com.ryduzz.visualkeystrokes.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Method;

public final class InputUtilCompat {
    private static final Method IS_KEY_PRESSED_WINDOW = findMethod(InputUtil.class, "isKeyPressed", Window.class, int.class);
    private static final Method IS_KEY_PRESSED_HANDLE = findMethod(InputUtil.class, "isKeyPressed", long.class, int.class);

    private InputUtilCompat() {
    }

    public static boolean isKeyPressed(MinecraftClient client, int keyCode) {
        try {
            if (IS_KEY_PRESSED_WINDOW != null) {
                return (boolean) IS_KEY_PRESSED_WINDOW.invoke(null, client.getWindow(), keyCode);
            }
            if (IS_KEY_PRESSED_HANDLE != null) {
                return (boolean) IS_KEY_PRESSED_HANDLE.invoke(null, client.getWindow().getHandle(), keyCode);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to query key state", e);
        }
        return GLFW.glfwGetKey(client.getWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
    }

    private static Method findMethod(Class<?> owner, String name, Class<?>... params) {
        try {
            return owner.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
