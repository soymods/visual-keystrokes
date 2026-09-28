package com.soyboy.visualkeystrokes.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.soyboy.visualkeystrokes.platform.VisualKeystrokesPlatform;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class OverlayConfig {
    private static final String CONFIG_DIR = "visualkeystrokes";
    private static final String CONFIG_FILE = "visualkeystrokes.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int CURRENT_CONFIG_VERSION = 2;
    private static final float DEFAULT_PRESSED_OPACITY = 0.25f;
    private static final int DEFAULT_SNAP_THRESHOLD = 6;

    public int configVersion = CURRENT_CONFIG_VERSION;
    public boolean enabled = true;
    public float scale = 1.0f;
    public int offsetX = 0;
    public int offsetY = 0;
    public int backgroundColor = 0xAA000000;
    public int pressedColor = 0xAA444444;
    public float pressedOpacity = DEFAULT_PRESSED_OPACITY;
    public int borderColor = 0xFF000000;
    public int textColor = 0xFFFFFFFF;
    public boolean snappingEnabled = true;
    public boolean guidesEnabled = true;
    public boolean distanceLabelsEnabled = true;
    public int snapThreshold = DEFAULT_SNAP_THRESHOLD;
    public List<KeyDefinition> keys = defaultKeys();

    public static OverlayConfig loadOrCreate() {
        Path path = configPath();
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                OverlayConfig config = GSON.fromJson(reader, OverlayConfig.class);
                if (config == null) {
                    config = new OverlayConfig();
                }
                if (config.ensureDefaults()) {
                    save(config);
                }
                return config;
            } catch (IOException e) {
                System.err.println("Failed to read VisualKeystrokes config: " + e.getMessage());
            }
        }

        OverlayConfig config = new OverlayConfig();
        save(config);
        return config;
    }

    public static void save(OverlayConfig config) {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to write VisualKeystrokes config: " + e.getMessage());
        }
    }

    public static int resolveColor(Integer override, int fallback) {
        return override == null ? fallback : override;
    }

    public void resetLayout() {
        scale = 1.0f;
        offsetX = 0;
        offsetY = 0;
        keys = defaultKeys();
    }

    private boolean ensureDefaults() {
        boolean changed = false;
        if (keys == null || keys.isEmpty()) {
            keys = defaultKeys();
            changed = true;
        }
        if (scale <= 0.0f) {
            scale = 1.0f;
            changed = true;
        }
        if (snapThreshold <= 0) {
            snapThreshold = DEFAULT_SNAP_THRESHOLD;
            changed = true;
        }
        if (configVersion < CURRENT_CONFIG_VERSION) {
            pressedOpacity = DEFAULT_PRESSED_OPACITY;
            configVersion = CURRENT_CONFIG_VERSION;
            changed = true;
        }
        if (Float.isNaN(pressedOpacity) || Float.isInfinite(pressedOpacity)) {
            pressedOpacity = DEFAULT_PRESSED_OPACITY;
            changed = true;
        }
        float clampedOpacity = Math.max(0.0f, Math.min(1.0f, pressedOpacity));
        if (Math.abs(clampedOpacity - pressedOpacity) > 1.0E-4f) {
            pressedOpacity = clampedOpacity;
            changed = true;
        }
        return changed;
    }

    private static Path configPath() {
        return VisualKeystrokesPlatform.getGameDir().resolve(CONFIG_DIR).resolve(CONFIG_FILE);
    }

    private static List<KeyDefinition> defaultKeys() {
        List<KeyDefinition> defaults = new ArrayList<>();
        defaults.add(new KeyDefinition("W", InputType.KEY, GLFW.GLFW_KEY_W, 38, 10, 24, 24, "wasd"));
        defaults.add(new KeyDefinition("A", InputType.KEY, GLFW.GLFW_KEY_A, 10, 38, 24, 24, "wasd"));
        defaults.add(new KeyDefinition("S", InputType.KEY, GLFW.GLFW_KEY_S, 38, 38, 24, 24, "wasd"));
        defaults.add(new KeyDefinition("D", InputType.KEY, GLFW.GLFW_KEY_D, 66, 38, 24, 24, "wasd"));
        defaults.add(new KeyDefinition("SPACE", InputType.KEY, GLFW.GLFW_KEY_SPACE, 10, 66, 80, 24, "space"));
        defaults.add(new KeyDefinition("SHIFT", InputType.KEY, GLFW.GLFW_KEY_LEFT_SHIFT, 10, 94, 52, 24, "shift"));
        defaults.add(new KeyDefinition("CTRL", InputType.KEY, GLFW.GLFW_KEY_LEFT_CONTROL, 66, 94, 52, 24, "ctrl"));
        defaults.add(new KeyDefinition("LMB", InputType.MOUSE, GLFW.GLFW_MOUSE_BUTTON_LEFT, 106, 10, 32, 32, "lmb"));
        defaults.add(new KeyDefinition("RMB", InputType.MOUSE, GLFW.GLFW_MOUSE_BUTTON_RIGHT, 142, 10, 32, 32, "rmb"));
        defaults.add(new KeyDefinition("MMB", InputType.MOUSE, GLFW.GLFW_MOUSE_BUTTON_MIDDLE, 106, 46, 32, 24, "mmb"));
        defaults.add(new KeyDefinition("CPS", "cps", 142, 46, 64, 28, "cps"));
        return defaults;
    }

    public enum InputType {
        KEY,
        MOUSE,
        STAT
    }

    public static final class KeyDefinition {
        public String label;
        public InputType type;
        public int code;
        public String statId;
        public int x;
        public int y;
        public int width;
        public int height;
        public String group;
        public Integer backgroundColorOverride;
        public Integer pressedColorOverride;
        public Integer borderColorOverride;
        public Integer textColorOverride;
        public Float pressedOpacityOverride;
        public Boolean visible;

        public KeyDefinition(String label, InputType type, int code, int x, int y, int width, int height, String group) {
            this.label = label;
            this.type = type;
            this.code = code;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.group = group;
            this.visible = true;
        }

        public KeyDefinition(String label, String statId, int x, int y, int width, int height, String group) {
            this.label = label;
            this.type = InputType.STAT;
            this.code = 0;
            this.statId = statId;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.group = group;
            this.visible = true;
        }

        public boolean isVisible() {
            return visible == null || visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }
}
