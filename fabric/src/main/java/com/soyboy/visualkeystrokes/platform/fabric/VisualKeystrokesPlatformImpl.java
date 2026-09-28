package com.soyboy.visualkeystrokes.platform.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * Fabric implementation of {@link com.soyboy.visualkeystrokes.platform.VisualKeystrokesPlatform}.
 */
public final class VisualKeystrokesPlatformImpl {
    private VisualKeystrokesPlatformImpl() {
    }

    public static Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
