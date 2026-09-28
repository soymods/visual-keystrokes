package com.soyboy.visualkeystrokes.platform.neoforge;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * NeoForge implementation of {@link com.soyboy.visualkeystrokes.platform.VisualKeystrokesPlatform}.
 */
public final class VisualKeystrokesPlatformImpl {
    private VisualKeystrokesPlatformImpl() {
    }

    public static Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
