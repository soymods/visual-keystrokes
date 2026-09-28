package com.soyboy.visualkeystrokes.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

/**
 * Loader-specific services. Architectury rewrites each call to
 * {@code com.soyboy.visualkeystrokes.platform.<loader>.VisualKeystrokesPlatformImpl}.
 */
public final class VisualKeystrokesPlatform {
    private VisualKeystrokesPlatform() {
    }

    @ExpectPlatform
    public static Path getGameDir() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modId) {
        throw new AssertionError();
    }
}
