package com.soyboy.visualkeystrokes.util;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class KeyBindingCompat {
    private static final String LEGACY_CATEGORY = "key.categories.visualkeystrokes";

    private KeyBindingCompat() {
    }

    public static KeyBinding createKeyBinding(String translationKey, InputUtil.Type type, int code) {
        List<String> signatures = new ArrayList<>();
        Exception lastError = null;
        for (Constructor<?> ctor : KeyBinding.class.getConstructors()) {
            signatures.add(signatureOf(ctor));
            try {
                KeyBinding keyBinding = tryCreate(ctor, translationKey, type, code);
                if (keyBinding != null) {
                    return keyBinding;
                }
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
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
            if (isInputTypeClass(param)) {
                args[i] = resolveInputType(param, type);
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

        // At runtime with intermediary names, Yarn names like "MISC" and "create" don't exist.
        // Fall back to structural detection: enum constants or any public static field of this type.
        if (categoryClass.isEnum()) {
            Object[] constants = categoryClass.getEnumConstants();
            if (constants != null && constants.length > 0) {
                return constants[0];
            }
        }
        for (Field field : categoryClass.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && categoryClass.isAssignableFrom(field.getType())) {
                Object value = field.get(null);
                if (value != null) {
                    return value;
                }
            }
        }

        throw new NoSuchMethodException("No supported key binding category creation path in " + categoryClass.getName());
    }

    private static Object createInputKey(InputUtil.Type type, int code) throws ReflectiveOperationException {
        Method createFromCode = type.getClass().getMethod("createFromCode", int.class);
        return createFromCode.invoke(type, code);
    }

    private static Object resolveInputType(Class<?> targetClass, InputUtil.Type fallback) throws ReflectiveOperationException {
        if (targetClass.isInstance(fallback)) {
            return fallback;
        }
        String fallbackName = ((Enum<?>) fallback).name();
        if (targetClass.isEnum()) {
            Object[] constants = targetClass.getEnumConstants();
            for (Object constant : constants) {
                if (constant instanceof Enum<?> enumConstant && enumConstant.name().equals(fallbackName)) {
                    return constant;
                }
            }
        }
        for (String fieldName : new String[]{fallbackName, "KEYSYM", "SCANCODE", "MOUSE"}) {
            try {
                Object value = targetClass.getField(fieldName).get(null);
                if (value != null) {
                    return value;
                }
            } catch (NoSuchFieldException ignored) {
                // Try next field.
            }
        }
        throw new NoSuchMethodException("No supported input type mapping for " + targetClass.getName());
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
        // Exclude types already handled by other branches in tryCreate().
        if (type.isPrimitive() || type == String.class) {
            return false;
        }
        if (isInputTypeClass(type) || isInputKeyClass(type)) {
            return false;
        }
        // Any remaining non-primitive object type is treated as a category.
        // The Yarn-name checks below are only reachable in a dev environment;
        // at runtime all names are intermediary (e.g. class_11900), so the
        // catch-all above is what actually fires in production.
        return true;
    }

    private static boolean isInputTypeClass(Class<?> type) {
        if (type == InputUtil.Type.class) {
            return true;
        }
        if (type.isPrimitive() || type == String.class) {
            return false;
        }
        return hasMethod(type, "createFromCode", int.class) && hasMethod(type, "name");
    }

    private static boolean isInputKeyClass(Class<?> type) {
        if (type.isPrimitive() || type == String.class) {
            return false;
        }
        if (type.getName().equals("net.minecraft.client.util.InputUtil$Key")) {
            return true;
        }
        return hasMethod(type, "getCode") && hasMethod(type, "getCategory");
    }

    private static boolean hasMethod(Class<?> owner, String name, Class<?>... params) {
        try {
            owner.getMethod(name, params);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
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
