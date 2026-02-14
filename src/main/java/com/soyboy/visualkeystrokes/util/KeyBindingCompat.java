package com.soyboy.visualkeystrokes.util;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class KeyBindingCompat {
    private static final String LEGACY_CATEGORY = "key.categories.visualkeystrokes";
    private static final String CATEGORY_CLASS = "net.minecraft.client.option.KeyBinding$Category";
    private static final String INPUT_KEY_CLASS = "net.minecraft.client.util.InputUtil$Key";

    private KeyBindingCompat() {
    }

    public static KeyBinding createKeyBinding(String translationKey, InputUtil.Type type, int code) {
        List<String> signatures = new ArrayList<>();
        ReflectiveOperationException lastError = null;
        for (Constructor<?> ctor : KeyBinding.class.getConstructors()) {
            signatures.add(signatureOf(ctor));
            try {
                KeyBinding keyBinding = tryCreate(ctor, translationKey, type, code);
                if (keyBinding != null) {
                    return keyBinding;
                }
            } catch (ReflectiveOperationException e) {
                lastError = e;
            }
        }
        if (lastError != null) {
            throw new IllegalStateException("Failed to create key binding. Available: " + signatures, lastError);
        }
        throw new IllegalStateException("No supported KeyBinding constructor found. Available: " + signatures);
    }

    private static KeyBinding tryCreate(
        Constructor<?> ctor,
        String translationKey,
        InputUtil.Type type,
        int code
    ) throws ReflectiveOperationException {
        Class<?>[] params = ctor.getParameterTypes();
        if (params.length < 2 || params[0] != String.class) {
            return null;
        }

        Object[] args = new Object[params.length];
        args[0] = translationKey;
        boolean usedCode = false;

        for (int i = 1; i < params.length; i++) {
            Class<?> param = params[i];
            if (param == InputUtil.Type.class) {
                args[i] = type;
                continue;
            }
            if (param == int.class) {
                args[i] = usedCode ? 0 : code;
                usedCode = true;
                continue;
            }
            if (param == String.class) {
                args[i] = LEGACY_CATEGORY;
                continue;
            }
            if (isInputKeyClass(param)) {
                args[i] = createInputKey(type, code);
                continue;
            }
            if (isCategoryClass(param)) {
                args[i] = createModernCategory(param);
                continue;
            }
            return null;
        }

        return (KeyBinding) ctor.newInstance(args);
    }

    private static Object createModernCategory(Class<?> categoryClass) throws ReflectiveOperationException {
        Identifier id = createIdentifier("visualkeystrokes", "general");

        Method createById = findStatic(categoryClass, "create", Identifier.class);
        if (createById != null) {
            return createById.invoke(null, id);
        }

        Method createByString = findStatic(categoryClass, "create", String.class);
        if (createByString != null) {
            return createByString.invoke(null, LEGACY_CATEGORY);
        }

        try {
            Constructor<?> ctor = categoryClass.getDeclaredConstructor(Identifier.class);
            ctor.setAccessible(true);
            return ctor.newInstance(id);
        } catch (NoSuchMethodException ignored) {
            // Fallback to predefined category constants below.
        }

        for (String name : new String[]{"MISC", "GAMEPLAY", "UI"}) {
            try {
                return categoryClass.getField(name).get(null);
            } catch (NoSuchFieldException ignored) {
                // Try next name.
            }
        }

        throw new NoSuchMethodException("No supported key binding category creation path in " + categoryClass.getName());
    }

    private static Object createInputKey(InputUtil.Type type, int code) throws ReflectiveOperationException {
        Method createFromCode = type.getClass().getMethod("createFromCode", int.class);
        return createFromCode.invoke(type, code);
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

    private static boolean isCategoryClass(Class<?> type) {
        return CATEGORY_CLASS.equals(type.getName());
    }

    private static boolean isInputKeyClass(Class<?> type) {
        return INPUT_KEY_CLASS.equals(type.getName());
    }

    private static String signatureOf(Constructor<?> ctor) {
        StringBuilder builder = new StringBuilder("KeyBinding(");
        Class<?>[] params = ctor.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(params[i].getSimpleName());
        }
        builder.append(')');
        return builder.toString();
    }
}
