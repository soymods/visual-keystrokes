package com.ryduzz.visualkeystrokes.util;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class KeyBindingCompat {
    private static final String LEGACY_CATEGORY = "key.categories.visualkeystrokes";

    private KeyBindingCompat() {
    }

    public static KeyBinding createKeyBinding(String translationKey, InputUtil.Type type, int code) {
        Constructor<?> legacyCtor = null;
        Constructor<?> modernCtor = null;
        for (Constructor<?> ctor : KeyBinding.class.getConstructors()) {
            Class<?>[] params = ctor.getParameterTypes();
            if (params.length == 4
                && params[0] == String.class
                && params[1] == InputUtil.Type.class
                && params[2] == int.class) {
                if (params[3] == String.class) {
                    legacyCtor = ctor;
                } else if ("net.minecraft.client.option.KeyBinding$Category".equals(params[3].getName())) {
                    modernCtor = ctor;
                }
            }
        }

        if (modernCtor != null) {
            try {
                Object category = createModernCategory(modernCtor.getParameterTypes()[3]);
                return (KeyBinding) modernCtor.newInstance(translationKey, type, code, category);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create modern key binding category.", e);
            }
        }

        if (legacyCtor != null) {
            try {
                return (KeyBinding) legacyCtor.newInstance(translationKey, type, code, LEGACY_CATEGORY);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create legacy key binding category.", e);
            }
        }

        throw new IllegalStateException("No supported KeyBinding constructor found.");
    }

    private static Object createModernCategory(Class<?> categoryClass) throws ReflectiveOperationException {
        Method create = categoryClass.getMethod("create", Identifier.class);
        return create.invoke(null, createIdentifier("visualkeystrokes", "general"));
    }

    private static Identifier createIdentifier(String namespace, String path) throws ReflectiveOperationException {
        Method of = findStatic(Identifier.class, "of", String.class, String.class);
        if (of != null) {
            return (Identifier) of.invoke(null, namespace, path);
        }
        Method tryParse = findStatic(Identifier.class, "tryParse", String.class);
        if (tryParse != null) {
            return (Identifier) tryParse.invoke(null, namespace + ":" + path);
        }
        Constructor<Identifier> ctor = Identifier.class.getDeclaredConstructor(String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(namespace, path);
    }

    private static Method findStatic(Class<?> owner, String name, Class<?>... params) {
        try {
            return owner.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
