package com.ryduzz.visualkeystrokes.input;

import com.ryduzz.visualkeystrokes.config.OverlayConfig;
import com.ryduzz.visualkeystrokes.screen.VisualKeystrokesEditor;
import com.ryduzz.visualkeystrokes.util.InputUtilCompat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public final class InputTracker {
    private final MinecraftClient client;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean middlePressed;
    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private final Deque<Long> middleClicks = new ArrayDeque<>();

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

        pruneOldClicks(leftClicks, now);
        pruneOldClicks(rightClicks, now);
        pruneOldClicks(middleClicks, now);
    }

    public String getStatValue(String statId) {
        return switch (statId) {
            case "cps" -> Integer.toString(leftClicks.size());
            case "cps_rmb" -> Integer.toString(rightClicks.size());
            case "cps_mmb" -> Integer.toString(middleClicks.size());
            default -> "0";
        };
    }

    private void pruneOldClicks(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000) {
            clicks.removeFirst();
        }
    }
}
