package com.soyboy.visualkeystrokes.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public final class TextCompatibilityBridge {
    private static final Method TRANSLATABLE = resolveStatic("translatable", String.class);

    private TextCompatibilityBridge() {
    }

    public static MutableText empty() {
        return Text.of("").copy();
    }

    public static MutableText copy(Text text) {
        if (text == null) {
            return empty();
        }
        return text.copy();
    }

    public static MutableText literal(String text) {
        return Text.of(text == null ? "" : text).copy();
    }

    public static MutableText translatableWithFallback(String key, String fallback) {
        if (TRANSLATABLE != null) {
            try {
                Object result = TRANSLATABLE.invoke(null, key);
                if (result instanceof Text text) {
                    MutableText mutable = copy(text);
                    if (key != null && key.equals(mutable.getString())) {
                        return literal(fallback);
                    }
                    return mutable;
                }
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return literal(fallback);
    }

    private static Method resolveStatic(String name, Class<?>... params) {
        try {
            Method method = Text.class.getMethod(name, params);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
