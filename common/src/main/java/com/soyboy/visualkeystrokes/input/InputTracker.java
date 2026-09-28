package com.soyboy.visualkeystrokes.input;

import com.soyboy.visualkeystrokes.config.OverlayConfig;
import com.soyboy.visualkeystrokes.screen.VisualKeystrokesEditor;
import com.soyboy.visualkeystrokes.util.InputUtilCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public final class InputTracker {
    private static final long ONE_SECOND_MS = 1000L;
    private final MinecraftClient client;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean middlePressed;
    private final boolean[] pressedKeys = new boolean[GLFW.GLFW_KEY_LAST + 1];
    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private final Deque<Long> middleClicks = new ArrayDeque<>();
    private String lastPressedKey = "-";

    public InputTracker(MinecraftClient client) {
        this.client = client;
    }

    public boolean isPressed(OverlayConfig.InputType type, int code) {
        if (client.currentScreen instanceof GameMenuScreen || client.currentScreen instanceof VisualKeystrokesEditor) {
            return false;
        }
        if (type == OverlayConfig.InputType.STAT) {
            return false;
        }
        if (type == OverlayConfig.InputType.MOUSE) {
            long handle = client.getWindow().getHandle();
            return GLFW.glfwGetMouseButton(handle, code) == GLFW.GLFW_PRESS;
        }
        return InputUtilCompat.isKeyPressed(client, code);
    }

    public void update() {
        if (client.currentScreen instanceof GameMenuScreen || client.currentScreen instanceof VisualKeystrokesEditor) {
            leftPressed = false;
            rightPressed = false;
            middlePressed = false;
            clearPressedKeys();
            return;
        }

        long handle = client.getWindow().getHandle();
        boolean left = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean right = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean middle = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        long now = System.currentTimeMillis();
        if (left && !leftPressed) {
            leftClicks.addLast(now);
        }
        if (right && !rightPressed) {
            rightClicks.addLast(now);
        }
        if (middle && !middlePressed) {
            middleClicks.addLast(now);
        }

        leftPressed = left;
        rightPressed = right;
        middlePressed = middle;

        updateKeyboardStats(handle, now);
        pruneOldClicks(leftClicks, now);
        pruneOldClicks(rightClicks, now);
        pruneOldClicks(middleClicks, now);
    }

    public String getStatValue(String statId) {
        return switch (statId) {
            case "cps" -> Integer.toString(leftClicks.size());
            case "cps_rmb" -> Integer.toString(rightClicks.size());
            case "cps_mmb" -> Integer.toString(middleClicks.size());
            case "last_key" -> lastPressedKey;
            default -> "0";
        };
    }

    private void updateKeyboardStats(long handle, long now) {
        for (int keyCode = GLFW.GLFW_KEY_SPACE; keyCode <= GLFW.GLFW_KEY_LAST; keyCode++) {
            boolean keyDown = GLFW.glfwGetKey(handle, keyCode) == GLFW.GLFW_PRESS;
            if (keyDown && !pressedKeys[keyCode]) {
                lastPressedKey = resolveKeyLabel(keyCode);
            }
            pressedKeys[keyCode] = keyDown;
        }
    }

    private void pruneOldClicks(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > ONE_SECOND_MS) {
            clicks.removeFirst();
        }
    }

    private void clearPressedKeys() {
        for (int i = 0; i < pressedKeys.length; i++) {
            pressedKeys[i] = false;
        }
    }

    private static String resolveKeyLabel(int keyCode) {
        String glfwName = GLFW.glfwGetKeyName(keyCode, 0);
        if (glfwName != null && !glfwName.isBlank()) {
            return glfwName.toUpperCase();
        }

        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_INSERT -> "INS";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_PAGE_UP -> "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "SCROLL";
            case GLFW.GLFW_KEY_NUM_LOCK -> "NUM";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "PRTSC";
            case GLFW.GLFW_KEY_PAUSE -> "PAUSE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_LEFT_SUPER -> "LWIN";
            case GLFW.GLFW_KEY_RIGHT_SUPER -> "RWIN";
            case GLFW.GLFW_KEY_MENU -> "MENU";
            default -> {
                if (keyCode >= GLFW.GLFW_KEY_F1 && keyCode <= GLFW.GLFW_KEY_F25) {
                    yield "F" + (keyCode - GLFW.GLFW_KEY_F1 + 1);
                }
                if (keyCode >= GLFW.GLFW_KEY_KP_0 && keyCode <= GLFW.GLFW_KEY_KP_9) {
                    yield "NUM" + (keyCode - GLFW.GLFW_KEY_KP_0);
                }
                yield "KEY " + keyCode;
            }
        };
    }
}
